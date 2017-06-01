package ru.runa.wfe.commons.email;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ru.runa.wfe.commons.email.EmailUtils.EmailsFilter;

public class EmailUtilsTest {
    
    private static final Logger LOG = Logger.getAnonymousLogger(); 
    
    @DataProvider
    public Object[][] getFilterEmailsData() {
        return new Object[][] {
            { Arrays.asList("user1@runawfe.org", "user2@runawfe.org"), null, null, 2 },
            { Arrays.asList("user1@runawfe.org", "user2@runawfe.org", "guest@runawfe.org"), "user*@runawfe.org", null, 2 },
            { Arrays.asList("user1@mail.ru", "USER2@runawfe.org", "guest@runawfe.org"), "*@runawfe.org", "guest@*", 1 },
            { Arrays.asList("user@runawfe.org", "super.user@runawfe.org",
                            "semjon.gorbunrkov@runawfe.org", "kisa.vorobjaninov@mail.ru", "shurik@mail.ru"),
                "*.*@runawfe.org,*@mail.ru", "*user*@runawfe.org,*.*@mail.ru", 2 },
            { Arrays.asList("user1@runawfe.org", "user2@runawfe.org", "user11@runawfe.org"),
                "user?@runawfe.org", null, 2 }
        };
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
	public void testFilterEmails(List<String> emails,
	        String includeFilterStr, String excludeFilterStr,
	        int filteredCount) {
	    
	    EmailsFilter includeFilter = EmailUtils.validateAndCreateEmailsFilter(includeFilterStr);
	    EmailsFilter excludeFilter = EmailUtils.validateAndCreateEmailsFilter(excludeFilterStr);
	    
	    List<String> filteredEmails;
	    filteredEmails = EmailUtils.filterEmails(emails, includeFilter, excludeFilter);
	    
	    LOG.info(filteredEmails.toString());
	    
	    assertEquals(filteredEmails.size(), filteredCount);
	}

}
