package io.github.guojiaxing1995.easyJmeter.service;

import io.github.guojiaxing1995.easyJmeter.dto.book.CreateOrUpdateBookDTO;
import io.github.guojiaxing1995.easyJmeter.model.BookDO;

import java.util.List;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
public interface BookService {

    boolean createBook(CreateOrUpdateBookDTO validator);

    List<BookDO> getBookByKeyword(String q);

    boolean updateBook(BookDO book, CreateOrUpdateBookDTO validator);

    BookDO getById(Integer id);

    List<BookDO> findAll();

    boolean deleteById(Integer id);

    List<BookDO> getBookByTitleKey(String q);
}
