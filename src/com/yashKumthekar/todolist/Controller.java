package com.yashKumthekar.todolist;

import com.yashKumthekar.todolist.dataModel.ToDoData;
import com.yashKumthekar.todolist.dataModel.ToDoItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    private List<ToDoItem>todoItems;
    @FXML
    private ListView<ToDoItem> todoListView;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadLineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<ToDoItem>filteredList;

    private Predicate<ToDoItem>wantAllItems;
    private Predicate<ToDoItem>wantTodaysItems;
    public void initialize(){
        /*ToDoItem item1 = new ToDoItem("Android App Developer Course" , "Planning to enroll in TimBuchalkas Android App Development Course",
                LocalDate.of(2021, Month.MAY,05));
        ToDoItem item2 = new ToDoItem("Data Structures And Algorithm" , "Planning to enroll in Abdul Bari Data Structures And Algorithm",
                LocalDate.of(2020, Month.DECEMBER,8));
        ToDoItem item3 = new ToDoItem("Birthday Of Mine" , "Planning to go to hotel if this corona ends",
                LocalDate.of(2020, Month.DECEMBER,8));
        ToDoItem item4 = new ToDoItem("Participating in Hackathon" , "Planning to take part in the various coding competitions",
                LocalDate.of(2020, Month.JANUARY,1));

        toDoItems = new ArrayList<>();
        toDoItems.add(item1);
        toDoItems.add(item2);
        toDoItems.add(item3);
        toDoItems.add(item4);
        ToDoData.getInstance().setToDoItems(toDoItems);
*/
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        listContextMenu.getItems().addAll(deleteMenuItem);
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observableValue, ToDoItem oldValue, ToDoItem newValue) {
                if(newValue !=null){
                    ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM yyyy");
                    deadLineLabel.setText(df.format(item.getDeadLine()));
                }
            }
        });

        wantAllItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem toDoItem) {
                return true;
            }
        };

        wantTodaysItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem toDoItem) {
                return (toDoItem.getDeadLine().equals(LocalDate.now()));
            }
        };
        filteredList = new FilteredList<ToDoItem>(ToDoData.getInstance().getToDoItems(),wantAllItems);

        SortedList<ToDoItem> sortedList = new SortedList<ToDoItem>(filteredList,
                new Comparator<ToDoItem>() {
                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getDeadLine().compareTo(o2.getDeadLine());
                    }
                });

        //todoListView.setItems(ToDoData.getInstance().getToDoItems());
        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> toDoItemListView) {
                ListCell<ToDoItem> cell = new ListCell<ToDoItem>(){
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }else{
                            setText(item.getShortDescription());
                            if(item.getDeadLine().equals(LocalDate.now())){
                                setTextFill(Color.RED);
                            }else if(item.getDeadLine().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.BLUE);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                    (obs,wasEmpty,isNowEmpty) -> {
                        if(isNowEmpty){
                            cell.setContextMenu(null);
                        }else{
                            cell.setContextMenu(listContextMenu);
                        }
                });

                return cell;
            }
        });
    }
    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo Item");
        dialog.setHeaderText("Use this dialog box to create a new item");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(loader.load());
        }catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            DialogController controller = loader.getController();
            ToDoItem newItem = controller.processResults();
         //   todoListView.getItems().setAll(ToDoData.getInstance().getToDoItems());
            todoListView.getSelectionModel().select(newItem);
            System.out.println("Ok pressed");
        }else{
            System.out.println("Cancel pressed");
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
        if(item!=null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteItem(item);
            }
        }
    }

    @FXML
    public void handleClickListView(){
        ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadLineLabel.setText(item.getDeadLine().toString());
        //System.out.println("The selected item is : "+ item);
        /*StringBuilder sb = new StringBuilder(item.getDetails());
        sb.append("\n\n\n");
        sb.append("Due : ");
        sb.append(item.getDeadLine().toString());
        itemDetailsTextArea.setText(sb.toString());*/
    }

    public void deleteItem(ToDoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete toDoItem");
        alert.setHeaderText("Delete Item : "+ item.getShortDescription());
        alert.setContentText("Are you sure? Press Ok to confirm, or cancel to Back");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)){
            ToDoData.getInstance().deleteTodoItem(item);
        }
    }

    public void handleFilterButton(){
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(wantTodaysItems);
            if(filteredList.isEmpty()){
                itemDetailsTextArea.clear();
                deadLineLabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                todoListView.getSelectionModel().select(selectedItem);
            }else{
                todoListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(selectedItem);
        }
    }
}
