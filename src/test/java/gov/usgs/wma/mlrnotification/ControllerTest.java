package gov.usgs.wma.mlrnotification;

import gov.usgs.wma.mlrnotification.model.Email;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@WebMvcTest(Controller.class)
@AutoConfigureMockMvc(secure=false)
@ActiveProfiles("test")
public class ControllerTest {	
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private EmailNotificationHandler emailHandler;
	
	private final String MOCK_ERROR_RESPONSE_500 = "error_500";
	private final String validEmailJsonWithSender = "{\"to\": [\"test@test.com\"], \"from\": \"test@test.net\", \"textBody\": \"test\", \"subject\": \"test\"}";
	private final String validEmailJsonWithoutSender = "{\"to\": [\"test@test.com\"], \"textBody\": \"test\", \"subject\": \"test\"}";
	private final String validEmailJsonWithoutSenderNoServer = "{\"to\": [\"test@test.com\"], \"textBody\": \"test2\", \"subject\": \"test\"}";
	private final String invalidEmailJson = "{\"from\": \"test@test.net\", \"textBody\": \"test\", \"subject\": \"test\"}";
	private final String malformedEmailJson = "{\"from\": \"test@test.net\" \"textBody\": \"test\", \"subject\": \"test\"}";
	private final String validEmailJsonWithOptional = "{\"to\": [\"test@test.com\"], \"from\": \"test@test.net\", \"htmlBody\": \"test\", \"subject\": \"test\", \"cc\": [\"test@test.com\"], \"bcc\": [\"test@test.com\"], \"replyTo\": \"test@test.net\"}";
	
	@Test
	public void testEmailControllerValidDataWithSender() throws Exception {
		given(emailHandler.sendEmail(any(Email.class))).willReturn(null);
		//Valid Subject, Message, Recipient, and Sender
		mvc.perform(post("/notification/email")
				.content(validEmailJsonWithSender)
				.contentType("application/json"))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testEmailControllerValidDataWithoutSender() throws Exception {
		given(emailHandler.sendEmail(any(Email.class))).willReturn(null);
		//Valid Subject, Message, Recipient, and Sender
		mvc.perform(post("/notification/email")
				.content(validEmailJsonWithoutSender)
				.contentType("application/json"))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testEmailControllerValidDataNoServer() throws Exception {
		given(emailHandler.sendEmail(any(Email.class))).willReturn(MOCK_ERROR_RESPONSE_500);
		
		//Valid Subject, Message, and Recipient
		MvcResult result = mvc.perform(post("/notification/email")
				.content(validEmailJsonWithoutSenderNoServer)
				.contentType("application/json"))
				.andDo(print())
				.andExpect(status().is5xxServerError())
				.andReturn();
		assertTrue(result.getResponse().getErrorMessage().contains(MOCK_ERROR_RESPONSE_500));
	}
	
	@Test
	public void testEmailControllerValidDataWithOptional() throws Exception {
		given(emailHandler.sendEmail(any(Email.class))).willReturn(null);
		//Valid Subject, Message, Recipient, and Sender
		mvc.perform(post("/notification/email")
				.content(validEmailJsonWithOptional)
				.contentType("application/json"))
				.andExpect(status().isOk());
	}

	@Test
	public void testEmailControllerInvalidEmail() throws Exception {
		given(emailHandler.sendEmail(any(Email.class))).willReturn(null);
		//Invalid Subject
		MvcResult result = mvc.perform(post("/notification/email")
				.content(malformedEmailJson)
				.contentType("application/json"))
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andReturn();
		assertTrue(result.getResponse().getStatus() == 400);
		
		MvcResult result2 = mvc.perform(post("/notification/email")
				.content(invalidEmailJson)
				.contentType("application/json"))
				.andDo(print())
				.andExpect(status().is4xxClientError())
				.andReturn();
		assertTrue(result2.getResponse().getErrorMessage().contains("No recipient email addresses provided."));
	}
}