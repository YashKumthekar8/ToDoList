package com.yashKumthekar.todolist.dataModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

public class ToDoData {
    private static ToDoData instance = new ToDoData();
    private static String filename = "ToDoListItem.txt";

    private ObservableList<ToDoItem> toDoItems;
    private DateTimeFormatter formatter;

    public static ToDoData getInstance(){
        return instance;
    }

    private ToDoData(){
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public ObservableList<ToDoItem> getToDoItems(){
        return toDoItems;
    }

    public void addToDoItem(ToDoItem item){
        toDoItems.add(item);
    }

    /*public void setToDoItems(List<ToDoItem> toDoItems) {
        this.toDoItems = toDoItems;
    }*/

    public void loadToDoItems()throws IOException {
        toDoItems = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        String input;
        try{
            while((input = br.readLine()) !=null){
                String[] itempPieces = input.split("\t");

                String shortDescription = itempPieces[0];
                String details = itempPieces[1];
                String dateString = itempPieces[2];

                LocalDate date = LocalDate.parse(dateString,formatter);
                ToDoItem toDoItem  = new ToDoItem(shortDescription,details,date);
                toDoItems.add(toDoItem);
            }
        }finally {
            if(br!=null){
                br.close();
            }
        }
    }

    public void storeToDoItems() throws IOException{
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);
        try{
            Iterator<ToDoItem> iter = toDoItems.iterator();
            while(iter.hasNext()){
                ToDoItem item = iter.next();
                bw.write(String.format("%s\t%s\t%s",
                                        item.getShortDescription(),
                                        item.getDetails(),
                                        item.getDeadLine().format(formatter)));
                bw.newLine();
            }
        }finally{
            if(bw!=null){
                bw.close();
            }
        }
    }

    public void deleteTodoItem(ToDoItem item){
        toDoItems.remove(item);
    }

}
