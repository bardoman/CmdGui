

import java.beans.*;
import java.io.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.*;
import javafx.stage.*;
import javafx.*;
import java.util.*;
import javafx.collections.*;

import javafx.beans.value.*;

public class CmdGui extends Application {
    String command;
    TextArea textArea = new TextArea();
    TextField textField;
    ArrayList<String> commandList = new ArrayList();
    String serFile="ser.xml";
    final ComboBox historyCombo = new ComboBox();
    int globalWidth=350;
    int textAreaDim=500;

    @Override
    public void start(Stage primaryStage) {
        serIn();
        if(commandList.size()==0)
        {
            commandList.add("");
        }
        textField=new TextField(commandList.get(0));
        

        updateCombo();

        Button execButton = new Button("Execute:");

        execButton.setMinWidth(110);

        execButton.setOnAction(new javafx.event.EventHandler<ActionEvent>() {

                                   @Override
                                   public void handle(ActionEvent event) {
                                       String tmp=textField.getText();
                                       doCommand(tmp);
                                   }
                               });

        Button clearButton = new Button("Clear:");

        clearButton.setMinWidth(110);

        clearButton.setOnAction(new javafx.event.EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        textArea.setText("");
                                    }
                                });

        GridPane gridPane = new GridPane();    

        gridPane.setMinSize(500, 500); 

        gridPane.setPadding(new Insets(10, 10, 10, 10));  

        gridPane.setVgap(5); 

        gridPane.setHgap(5);       

        gridPane.setAlignment(Pos.CENTER); 

        gridPane.add(execButton, 1,1);

        Label history = new Label("HISTORY:");

        gridPane.add(history, 1,2);

        gridPane.add(clearButton, 1,3);

        textField.setMinWidth(globalWidth);

        textField.setEditable(true);

        javafx.event.EventHandler<ActionEvent> event = new javafx.event.EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                doCommand(textField.getText());
            } 
        }; 

        // when enter is pressed 
        textField.setOnAction(event); 

        gridPane.add(textField, 3,1);

        textArea.setWrapText(true);

        textArea.setMinWidth(globalWidth);
        textArea.setMinHeight(globalWidth);

        historyCombo.setMinWidth(globalWidth);
        historyCombo.valueProperty().addListener(new ChangeListener<String>() {
                                                     public void changed(ObservableValue ov, String t, String t1) {
                                                         Thread thread = 
                                                         new Thread(new Runnable() {
                                                                        public void run() {
                                                                            textField.setText(historyCombo.getValue().toString());
                                                                            doCommand(historyCombo.getValue().toString());
                                                                        }
                                                                    });
                                                         thread.start();   
                                                     }
                                                 });


        gridPane.add(historyCombo,3,2);

        gridPane.add(textArea, 3, 3);

        Scene scene = new Scene(gridPane, textAreaDim, textAreaDim);

        primaryStage.setTitle("Command Executor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    void doCommand(String cmd) {
        updateCommandList(cmd);
        updateCombo();
        serOut();
        try {
            textArea.appendText("\n");
            textArea.appendText("************************************************************");
            textArea.appendText("\n");
            textArea.appendText(">"+cmd);
            textArea.appendText("\n");

            Process p0 = Runtime.getRuntime().exec("cmd /c "+cmd);
            p0.waitFor();
            if(p0.exitValue()==0) {
                String line0;
                BufferedReader in0 = new BufferedReader(new InputStreamReader(p0.getInputStream()));
                while((line0 = in0.readLine()) != null) {
                    textArea.appendText(line0);
                }
                in0.close();
            } else {
                String errLine0;
                BufferedReader err0 = new BufferedReader(new InputStreamReader(p0.getErrorStream()));
                while((errLine0 = err0.readLine()) != null) {
                    textArea.appendText(errLine0);
                }
                err0.close(); 
                //  System.exit(1);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void serIn() {
        try {
            File file = new File(serFile);
            if(file.exists()) {
                XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(serFile)));
                commandList = (ArrayList) d.readObject();
                d.close();
            } else serOut();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    void serOut() {
        try {
            XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(serFile)));
            e.writeObject(commandList);
            e.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void updateCombo() {
               
        ObservableList<String> oList = FXCollections.observableArrayList(commandList);

        historyCombo.setItems(oList);
    }

    void updateCommandList(String cmd) {
        if(!commandList.contains(cmd))
            commandList.add(cmd);
    }


}

