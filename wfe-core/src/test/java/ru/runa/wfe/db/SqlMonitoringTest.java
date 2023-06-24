package ru.runa.wfe.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.Test;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;

@Test
@ContextConfiguration(locations = { "classpath:ru/runa/wfe/db/test.context.xml" })
public class SqlMonitoringTest extends AbstractTransactionalTestNGSpringContextTests {

    static {
        System.setProperty("hibernate.show_sql", "true");
        System.setProperty("hibernate.format_sql", "true");
    }

    @Autowired
    ProcessDefinitionDao processDefinitionDao;

    @Test(enabled = false)
    public void test2() {
        System.out.println(" ----------------- ");
        ProcessDefinition d = processDefinitionDao.get(1L);
        System.out.println(d.getId());
        System.out.println(d.getCreateDate());
        System.out.println(d.getPack().getId());
        System.out.println(d.getPack().getName());
        System.out.println(d.getPack().getLatest().getId());
        System.out.println(d.getPack().getLatest().getCreateDate());
    }

}
