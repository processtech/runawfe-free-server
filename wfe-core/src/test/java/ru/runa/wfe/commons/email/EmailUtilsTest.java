package ru.runa.wfe.commons.email;

import java.util.Arrays;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.runa.wfe.commons.email.EmailUtils.EmailsFilter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EmailUtilsTest {

    @DataProvider
    public Object[][] getFilterEmailsData() {
        return new Object[][] {
                { Arrays.asList("user1@runawfe.org", "user2@runawfe.org"), null, null, 2 },
                { Arrays.asList("user1@runawfe.org", "user2@runawfe.org", "guest@runawfe.org"), "user*@runawfe.org", null, 2 },
                { Arrays.asList("user1@mail.ru", "USER2@runawfe.org", "guest@runawfe.org"), "*@runawfe.org", "guest@*", 1 },
                { Arrays.asList("user1@runawfe.org", "user2@runawfe.org", "user11@runawfe.org"), "user?@runawfe.org", null, 2 } };
    }

    @Test
    public void testIsEmailsFilterValid() {
        assertFalse(EmailsFilter.isEmailsFilterValid(""));
        assertFalse(EmailsFilter.isEmailsFilterValid("nothing"));
        assertFalse(EmailsFilter.isEmailsFilterValid("noserver@"));
        assertFalse(EmailsFilter.isEmailsFilterValid("@nologin"));
        assertTrue(EmailsFilter.isEmailsFilterValid("nice.user@nice_server.com"));
        assertTrue(EmailsFilter.isEmailsFilterValid("nice.*@????_server.c?m"));
        assertFalse(EmailsFilter.isEmailsFilterValid("no.UPPERs@Allowed"));
    }

    @Test(dataProvider = "getFilterEmailsData")
    public void testFilterEmails(List<String> emails, String includeEmail, String excludeEmail, int filteredCount) {
        EmailsFilter includeFilter = includeEmail != null ? EmailUtils.validateAndCreateEmailsFilter(Arrays.asList(includeEmail)) : null;
        EmailsFilter excludeFilter = excludeEmail != null ? EmailUtils.validateAndCreateEmailsFilter(Arrays.asList(excludeEmail)) : null;
        List<String> filteredEmails = EmailUtils.filterEmails(emails, includeFilter, excludeFilter);
        assertEquals(filteredEmails.size(), filteredCount);
    }

}
