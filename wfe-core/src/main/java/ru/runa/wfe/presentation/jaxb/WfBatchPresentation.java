package ru.runa.wfe.presentation.jaxb;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import ru.runa.wfe.presentation.ClassPresentationType;

@Data
public class WfBatchPresentation {
    private ClassPresentationType classPresentationType;
    private int pageSize = 100;
    private int pageNumber = 1;
    private List<Filter> filters = new ArrayList<>();
    private List<Sorting> sortings = new ArrayList<>();
    private List<String> variables = new ArrayList<>();

    @Data
    public static class Sorting {
        private String name;
        private Order order;

        public enum Order {
            asc,
            desc;
        }
    }

    @Data
    public static class Filter {
        private String name;
        private String value;
        private boolean exclusive;
    }

}
