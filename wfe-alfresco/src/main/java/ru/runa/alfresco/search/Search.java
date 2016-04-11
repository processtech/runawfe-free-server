package ru.runa.alfresco.search;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

import ru.runa.alfresco.LocalAlfConnection;

import com.google.common.base.Objects;

/**
 * Represents query object.
 * 
 * @author dofs
 */
public class Search extends Group {
    private List<Sorting> sortings = new ArrayList<Sorting>();
    /**
     * results limit.
     */
    private int limit;

    private StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

    public Search(N operand, Op operator, Object... params) {
        super(operand, operator, params);
    }

    public Search(String groupOperator, Group... expressions) {
        super(groupOperator, expressions);
    }

    public Search(String groupOperator, N operand, Op operator, Object... params) {
        super(groupOperator, operand, operator, params);
    }

    public boolean hasSorting() {
        return !sortings.isEmpty();
    }

    public void addSort(QName sortColumnName, boolean sortAscending) {
        sortings.add(new Sorting(sortColumnName, sortAscending));
    }

    public List<Sorting> getSortings() {
        return sortings;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * Limit results. Note that this implemented correctly only in
     * {@link LocalAlfConnection}.
     * 
     * @param limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    public StoreRef getStore() {
        return store;
    }

    public void setStore(StoreRef store) {
        this.store = store;
    }

    public class Sorting {
        private final QName name;
        private final boolean ascending;

        public Sorting(QName name, boolean ascending) {
            this.name = name;
            this.ascending = ascending;
        }

        public QName getName() {
            return name;
        }

        public boolean isAscending() {
            return ascending;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Sorting) {
                return Objects.equal(name, ((Sorting) obj).name);
            }
            return super.equals(obj);
        }
    }
}
