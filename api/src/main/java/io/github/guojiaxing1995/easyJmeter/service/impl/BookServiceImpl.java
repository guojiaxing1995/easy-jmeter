package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.dto.book.CreateOrUpdateBookDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.BookMapper;
import io.github.guojiaxing1995.easyJmeter.model.BookDO;
import io.github.guojiaxing1995.easyJmeter.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Override
    public boolean createBook(CreateOrUpdateBookDTO validator) {
        BookDO book = new BookDO();
        book.setAuthor(validator.getAuthor());
        book.setTitle(validator.getTitle());
        book.setImage(validator.getImage());
        book.setSummary(validator.getSummary());
        return bookMapper.insert(book) > 0;
    }

    @Override
    public List<BookDO> getBookByKeyword(String q) {
        List<BookDO> books = bookMapper.selectByTitleLikeKeyword(q);
        return books;
    }

    @Override
    public boolean updateBook(BookDO book, CreateOrUpdateBookDTO validator) {
        book.setAuthor(validator.getAuthor());
        book.setTitle(validator.getTitle());
        book.setImage(validator.getImage());
        book.setSummary(validator.getSummary());
        return bookMapper.updateById(book) > 0;
    }

    @Override
    public BookDO getById(Integer id) {
        BookDO book = bookMapper.selectById(id);
        return book;
    }

    @Override
    public List<BookDO> findAll() {
        List<BookDO> books = bookMapper.selectList(null);
        return books;
    }

    @Override
    public boolean deleteById(Integer id) {
        return bookMapper.deleteById(id) > 0;
    }

    @Override
    public List<BookDO> getBookByTitleKey(String q) {
        List<BookDO> books = bookMapper.selectByTitleKey(q);
        return books;
    }
}
