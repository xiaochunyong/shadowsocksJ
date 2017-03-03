package me.ely.shadowsocks.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import me.ely.shadowsocks.BootGUI;
import me.ely.shadowsocks.model.Config;
import me.ely.shadowsocks.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Ely on 09/12/2016.
 */
public class ServerPreferencesController {

    private static final Logger logger = LoggerFactory.getLogger(ServerPreferencesController.class);

    private BootGUI app;

    @FXML
    ListView<Server> listView;

    @FXML
    TextField serverField;

    @FXML
    TextField portField;

    @FXML
    ComboBox<String> cryptCombobox;

    @FXML
    PasswordField passwordField;

    @FXML
    TextField remarkField;

    private ObservableList<Server> configData = FXCollections.observableArrayList();
    private ObservableList<String> cryptList = FXCollections.observableArrayList();

    Server config;

    @FXML
    private void initialize() {
        cryptCombobox.setItems(cryptList);

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            config = newValue;
            showServerDetail(newValue);
        });
        listView.setCellFactory(new Callback<ListView<Server>, ListCell<Server>>() {
            @Override
            public ListCell<Server> call(ListView<Server> param) {
                return new ListCell<Server>() {
                    @Override
                    protected void updateItem(Server item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            this.setText(item.getRemark());
                        } else {
                            this.setText("");
                        }
                    }
                } ;
            }
        });
        listView.setItems(configData);

        initData();
    }

    private void initData() {
        cryptList.add("aes-cfb-256");

    }

    public void showServerDetail(Server config) {
        if (config != null) {
            serverField.setText(config.getHost());
            portField.setText(Integer.toString(config.getPort()));
            cryptCombobox.getSelectionModel().select(config.getMethod());
            passwordField.setText(config.getPassword());
            remarkField.setText(config.getRemark());
        } else {
            // Server is null, remove all the text.
            serverField.setText("");
            portField.setText("");
            cryptCombobox.getSelectionModel().select(null);
            passwordField.setText("");
            remarkField.setText("");
        }
    }


    @FXML
    public void handleOk() {
        config.setHost(serverField.getText());
        config.setPort(Integer.parseInt(portField.getText()));
        config.setMethod(cryptCombobox.getSelectionModel().getSelectedItem());
        config.setPassword(passwordField.getText());
        config.setRemark(remarkField.getText());

        if (configData.indexOf(config) == -1) {
            configData.add(config);
        }

        this.app.config.setConfigs(configData);
        Config.saveConfig(this.app.config);

        handleCancel();
    }

    @FXML
    public void handleNew() {
        config = new Server();
        showServerDetail(config);
    }

    @FXML
    public void handleDelete() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            configData.remove(selectedIndex);
        }
    }

    @FXML
    public void handleCancel() {
        app.hide();
    }


    public void setApp(BootGUI app) {
        this.app = app;
        this.configData.addAll(this.app.config.getConfigs());
        if (configData.size() > 0) {
            showServerDetail(configData.get(0));
        } else {
            config = new Server();
            showServerDetail(config);
        }
    }
}
