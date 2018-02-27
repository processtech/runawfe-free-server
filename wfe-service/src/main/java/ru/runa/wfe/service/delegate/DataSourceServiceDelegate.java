package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.service.DataSourceService;
import ru.runa.wfe.user.User;

public class DataSourceServiceDelegate extends EJB3Delegate implements DataSourceService {

    public DataSourceServiceDelegate() {
        super(DataSourceService.class);
    }

    private DataSourceService getDataSourceService() {
        return (DataSourceService) getService();
    }

    @Override
    public List<String> getNames() {
        try {
            return getDataSourceService().getNames();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void importDataSource(User user, byte[] archive) {
        try {
            getDataSourceService().importDataSource(user, archive);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] exportDataSource(User user, String name) {
        try {
            return getDataSourceService().exportDataSource(user, name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeDataSource(User user, String name) {
        getDataSourceService().removeDataSource(user, name);
    }

}
