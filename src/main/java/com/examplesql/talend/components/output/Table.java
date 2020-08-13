package com.examplesql.talend.components.output;

import java.io.Serializable;
import java.util.List;

public class Table implements Serializable {

    private final String catalog;

    private final String schema;

    private final String name;

    private final List<Field> columns;

    public Table(String catalog, String schema, String name, List<Field> columns)
    {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.columns = columns;
    }


    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public List<Field> getColumns() {
        return columns;
    }


    public static TableBuilder builder() {
        return new TableBuilder();
    }

    public static class TableBuilder {

        private String catalog;
        private String schema;
        private String name;
        private List<Field> columns;

        public TableBuilder catalog(String catalog)
        {
            this.catalog = catalog;
            return this;
        }

        public TableBuilder schema(String schema)
        {
            this.schema = schema;
            return this;
        }

        public TableBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        public TableBuilder columns(List<Field> columns)
        {
            this.columns = columns;
            return this;
        }


        public Table build() {
            return new Table(catalog, schema, name, columns);
        }
    }
}
