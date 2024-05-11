package com.example.library.controllers;

import com.example.library.common.Regex;
import com.example.library.models.Book;
import com.example.library.services.BookServiceImpl;
import com.example.library.services.IBookService;
import com.example.library.utils.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class BookManagementController implements Initializable {
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnAddCategory;
    @FXML
    private ComboBox<String> cbCategory;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnRefresh;
    @FXML
    private TextField txtBookId;
    @FXML
    private TextField txtBookName;
    @FXML
    private TextField txtCategory;
    @FXML
    private TextField txtQuantity;
    @FXML
    private DatePicker dpPublish;
    @FXML
    private ComboBox<String> cbAuthor;
    @FXML
    private TableView<Book> tbBooks;
    @FXML
    private TableColumn<Book, String> colBookId;
    @FXML
    private TableColumn<Book, String> colBookName;
    @FXML
    private TableColumn<Book, String> colAuthorName;
    @FXML
    private TableColumn<Book, String> colCategory;
    @FXML
    private TableColumn<Book, Integer> colQuantity;
    @FXML
    private TableColumn<Book, LocalDate> colPublishDate;

    private final IBookService bookService;
    private boolean isAddingCategory = false;

    public BookManagementController() {
        this.bookService = new BookServiceImpl();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtCategory.setVisible(false);
        cbCategory.setVisible(true);
        loadBooksOnTable();
        initComboBox();
        customDatePicker();
    }

    private void customDatePicker() {
        dpPublish.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isAfter(LocalDate.now()));
            }
        });
    }

    private void initComboBox() {
        cbAuthor.getItems().addAll(bookService.getAllAuthorName());
        cbCategory.getItems().addAll(bookService.getAllCategoryName());
    }

    private void loadBooksOnTable() {
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colBookName.setCellValueFactory(new PropertyValueFactory<>("bookName"));
        colAuthorName.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPublishDate.setCellValueFactory(new PropertyValueFactory<>("publisher"));

        tbBooks.setItems(bookService.getAllBook());
    }

    public void fillToTextField(MouseEvent mouseEvent) {
        Optional<Book> tblBook = Optional.ofNullable(tbBooks.getSelectionModel().getSelectedItem());

        tblBook.ifPresent(book -> {
            txtBookId.setText(book.getBookId());
            txtBookName.setText(book.getBookName());
            cbCategory.setValue(book.getCategory());
            txtQuantity.setText(String.valueOf(book.getQuantity()));
            dpPublish.setValue(book.getPublisher());
            cbAuthor.setValue(book.getAuthor());

        });

    }

    public void onClickAdd(ActionEvent actionEvent) {
        String bookId = txtBookId.getText();
        String bookName = txtBookName.getText();
        String category = txtCategory.getText().isBlank() ? cbCategory.getValue() : txtCategory.getText();
        String quantity = txtQuantity.getText();
        LocalDate publishDate = dpPublish.getValue();
        String author = cbAuthor.getValue();

        // validate
        if(!isValid(Regex.INTEGER_NUMBER, quantity)){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", null, "Số lượng sách phải là số nguyên");
            return;
        }


        // check null
        if (isNull()) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", null, "Hãy điền đầy đủ thông tin sách!");
            return;
        }
        Book book = Book.builder()
                .bookId(bookId)
                .bookName(bookName)
                .category(category)
                .quantity(Integer.parseInt(quantity))
                .publisher(publishDate)
                .author(author)
                .build();

        bookService.saveBook(book);

        tbBooks.setItems(bookService.getAllBook());

    }

    public void onClickDelete(ActionEvent actionEvent) {
        Optional<Book> selectedBook = Optional.ofNullable(tbBooks.getSelectionModel().getSelectedItem());

        if(selectedBook.isPresent() && AlertUtil.showConfirmation("Bạn có chắc chắn muốn xóa không?")){
            bookService.deleteBook(selectedBook.get());
        }
        else if(selectedBook.isEmpty()){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", null, "Hãy chọn sách cần xóa!");
        }
        tbBooks.setItems(bookService.getAllBook());
    }

    public void onClickUpdate(ActionEvent actionEvent) {
        Optional<Book> selectedBook = Optional.ofNullable(tbBooks.getSelectionModel().getSelectedItem());

        String bookId = txtBookId.getText();
        String bookName = txtBookName.getText();
        String category = txtCategory.getText().isBlank() ? cbCategory.getValue() : txtCategory.getText();
        String quantity = txtQuantity.getText();
        LocalDate publishDate = dpPublish.getValue();
        String author = cbAuthor.getValue();

        //check null
        if(isNull(bookId, bookName, category, quantity, publishDate, author)){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", null, "Hãy điền đầy đủ thông tin sách!");
            return;
        }

        //validate
        if(!isValid(Regex.INTEGER_NUMBER, quantity)){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", null, "Số lượng sách phải là số nguyên");
            return;
        }

        if(selectedBook.isPresent()){
            selectedBook.get().setBookName(bookName);
            selectedBook.get().setCategory(category);
            selectedBook.get().setQuantity(Integer.parseInt(quantity));
            selectedBook.get().setPublisher(publishDate);
            selectedBook.get().setAuthor(author);
            bookService.updateBook(selectedBook.get());
            tbBooks.setItems(bookService.getAllBook());
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", null, "Cập nhật sách thành công!");
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", null, "Hãy chọn sách cần cập nhật!");
        }

    }

    public void onClickRefresh(ActionEvent actionEvent) {
    }
    public void onClickAddCategory(ActionEvent actionEvent) {
        if (isAddingCategory) {
            txtCategory.setVisible(false);
            cbCategory.setVisible(true);
            txtCategory.clear();
            btnAddCategory.setText("Thêm mới");
            cbCategory.getItems().clear();
            cbCategory.getItems().addAll(bookService.getAllCategoryName());
        } else {
            txtCategory.setVisible(true);
            cbCategory.setVisible(false);
            cbCategory.getSelectionModel().clearSelection();
            btnAddCategory.setText("Hủy");
        }
        isAddingCategory = !isAddingCategory;


    }


    private boolean isNull(Object... objects) {
        for (Object obj : objects) {
            if (obj == null || obj.toString().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private void clear(){
        txtBookId.clear();
        txtBookName.clear();
        txtCategory.clear();
        txtQuantity.clear();
        dpPublish.setValue(null);
        cbAuthor.setValue(null);
        tbBooks.getSelectionModel().clearSelection();
    }

    private boolean isValid(String regex, String input) {
        return input.matches(regex);
    }


    public void onSearch(KeyEvent keyEvent) {
        String keyword = txtSearch.getText();
        if(keyword.isEmpty()){
            tbBooks.setItems(bookService.getAllBook());
            return;
        } else {
            tbBooks.setItems(bookService.getAllBook().filtered(book -> {
                // search by bookId, bookName, author, category
                return book.getBookId().toLowerCase().contains(keyword.toLowerCase()) ||
                        book.getBookName().toLowerCase().contains(keyword.toLowerCase()) ||
                        book.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                        book.getCategory().toLowerCase().contains(keyword.toLowerCase());
            }));
        }
    }
}