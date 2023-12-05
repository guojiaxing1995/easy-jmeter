package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.github.guojiaxing1995.easyJmeter.dto.book.CreateOrUpdateBookDTO;
import io.github.guojiaxing1995.easyJmeter.model.BookDO;
import io.github.guojiaxing1995.easyJmeter.service.BookService;
import io.github.guojiaxing1995.easyJmeter.vo.CreatedVO;
import io.github.guojiaxing1995.easyJmeter.vo.DeletedVO;
import io.github.guojiaxing1995.easyJmeter.vo.UpdatedVO;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
@RestController
@RequestMapping("/v1/book")
@Validated
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/{id}")
    public BookDO getBook(@PathVariable(value = "id") @Positive(message = "{id.positive}") Integer id) {
        BookDO book = bookService.getById(id);
        if (book == null) {
            throw new NotFoundException(10022);
        }
        return book;
    }

    @GetMapping("")
    @GroupRequired
    @PermissionMeta(value = "图书管理", module = "图书")
    public List<BookDO> getBooks() {
        List<BookDO> books = bookService.findAll();
        return books;
    }


    @GetMapping("/search")
    public List<BookDO> searchBook(@RequestParam(value = "q", required = false, defaultValue = "") String q) {
        List<BookDO> books = bookService.getBookByKeyword("%" + q + "%");
        return books;
    }

    @GetMapping("/title")
    public List<BookDO> searchTitle(@RequestParam(value = "q", required = false, defaultValue = "") String q){
        List<BookDO> books = bookService.getBookByTitleKey(q);
        return books;
    }

    @PostMapping("")
    public CreatedVO createBook(@RequestBody @Validated CreateOrUpdateBookDTO validator) {
        bookService.createBook(validator);
        return new CreatedVO(12);
    }


    @PutMapping("/{id}")
    public UpdatedVO updateBook(@PathVariable("id") @Positive(message = "{id.positive}") Integer id, @RequestBody @Validated CreateOrUpdateBookDTO validator) {
        BookDO book = bookService.getById(id);
        if (book == null) {
            throw new NotFoundException(10022);
        }
        bookService.updateBook(book, validator);
        return new UpdatedVO(13);
    }


    @DeleteMapping("/{id}")
    @GroupRequired
    @PermissionMeta(value = "删除图书", module = "图书")
    public DeletedVO deleteBook(@PathVariable("id") @Positive(message = "{id.positive}") Integer id) {
        BookDO book = bookService.getById(id);
        if (book == null) {
            throw new NotFoundException();
        }
        bookService.deleteById(book.getId());
        return new DeletedVO(14);
    }

}
