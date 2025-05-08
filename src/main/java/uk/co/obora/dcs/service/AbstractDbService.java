package uk.co.obora.dcs.service;

public abstract class AbstractDbService<T> {

    public abstract void save(T item);
    public abstract void delete(T item);
}
