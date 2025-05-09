package uk.co.obora.dcs.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public abstract class AbstractDbService<T> {

    private final int itemsPerPage;

    public Stream<T> find(int offset, int limit) throws Throwable {
        Pageable pageable = PageRequest.of(getPageNumber(offset), limit);
        return findPage(pageable).stream();
    }

    private int getPageNumber(int offset) {
        return itemsPerPage % (offset > 0 ? offset : 1);
    }

    public abstract void save(T item) throws Throwable;
    public abstract void delete(T item) throws Throwable;
    public abstract Page<T> findPage(Pageable pageable) throws Throwable;
    public abstract Long getCount() throws Throwable;
}
