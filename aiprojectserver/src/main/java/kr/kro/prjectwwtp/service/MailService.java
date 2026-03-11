package kr.kro.prjectwwtp.service;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import kr.kro.prjectwwtp.domain.FlowPredict;

//import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
//import com.amazonaws.services.simpleemail.model.Body;
//import com.amazonaws.services.simpleemail.model.Content;
//import com.amazonaws.services.simpleemail.model.Destination;
//import com.amazonaws.services.simpleemail.model.Message;
//import com.amazonaws.services.simpleemail.model.RawMessage;
//import com.amazonaws.services.simpleemail.model.SendEmailRequest;
//import com.amazonaws.services.simpleemail.model.SendEmailResult;
//import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
//import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

//import jakarta.activation.DataHandler;
//import jakarta.mail.Session;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeBodyPart;
//import jakarta.mail.internet.MimeMessage;
//import jakarta.mail.internet.MimeMultipart;
//import jakarta.mail.util.ByteArrayDataSource;
import kr.kro.prjectwwtp.domain.Member;
import kr.kro.prjectwwtp.domain.TmsPredict;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {
	private final SendGrid sendGrid;
	private final LogService logService;
	@Value("${spring.sendgrid.api-key}")
    private String apiKey;

    @Value("${spring.sendgrid.from-email}")
    private String fromEmail;	
	
	@Value("${spring.EmailAPI.URI}")
	private String emailAPIDomain;
    
    public void sendValidateEmail(Member member) {
    	String type = "send One";
		String errorMsg = null;
    	try {
	    	String userId = member.getUserId();
			String email = member.getUserEmail();
			String subject = "Email 인증 From FlowWater"; 
			String validateLink = emailAPIDomain + "/api/member/validateKey?keyValue="+member.getValidateKey();
			String deleteLink = emailAPIDomain + "/api/member/deleteEmail?userId="+userId+"&email="+email;
			String body = "<div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; height: 600px; border-top: 4px solid #3498db; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">" +
		              "    <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
		              "        <span style=\"color: #3498db;\">" + subject + "</span> 안내" +
		              "    </h1>" +
		              "    <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
		              "        아래 버튼을 클릭하여 인증을 완료해 주세요.<br>" +
		              "        본 메일은 <b>FlowWater</b> 서비스 이용을 위해 발송되었습니다.<br>" +
		              "        본 메일의 인증은 10분 간만 유효합니다." +
		              "    </p>" +
		              "<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px;'> " +
		              "  <tr> " +
		              "    <td align='center' bgcolor='#3498db' width='210' height='45' style='border-radius: 5px; color: #ffffff;'> " +
		              "      <a href='" + validateLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'> " +
		              "        인증 완료하기 " +
		              "      </a> " +
		              "    </td> " +
		              "  </tr> " +
		              "</table>" +	 		              
		              "    <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
		              "        더이상 이 보고서를 받지 않으시려면<br>" +
		              "        아래 버튼을 눌러 이메일 정보를 삭제하십시오..<br>" +
		              "    </p>" +
		              "<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px;'> " +
		              "  <tr> " +
		              "    <td align='center' bgcolor='#ff4444' width='210' height='45' style='border-radius: 5px; color: #ffffff;'> " +
		              "      <a href='" + deleteLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'> " +
		              "        수신거부 " +
		              "      </a> " +
		              "    </td> " +
		              "  </tr> " +
		              "</table>" +	 		              
		              "    <div style=\"border-top: 1px solid #DDD; padding: 5px;\">" +
					  "        <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">" +
					  "            도움이 필요하시면 <a href=\"https://www.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다." +
					  "        </p>" +
		              "    </div>" +
		              "</div>";
			Response response = sendEmail(member.getUserEmail(), subject, body);

			System.out.println("sendEmail response : " + response.getStatusCode());
			if(response.getStatusCode() != 202)
				errorMsg = response.getBody();
    	}catch(Exception e) {
			errorMsg = e.getMessage();
			logService.addErrorLog("MailService.java", "sendEmail()", e.getMessage());
		} finally {
			logService.addMailLog(member, type, errorMsg);	
		}
    }
    
	public String failMessage(String type, String errorMsg) {
		String titleText = "FlowWater 인증 실패 안내";
		String body = "<!DOCTYPE html>" +
			    "<html>" +
			    "<head>" +
			    "    <meta charset=\"UTF-8\">" +
			    "    <title>" + titleText + "</title>" + // 브라우저 탭 타이틀
			    "</head>" +
			    "<body style=\"margin: 0; padding: 0;\">" +
			    "    <div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; border-top: 4px solid #e74c3c; margin: 50px auto; padding: 30px 0; box-sizing: border-box;\">" +
			    "        <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
			    "            <span style=\"color: #e74c3c;\">인증 실패</span> 안내" +
			    "        </h1>" +
			    "        <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
			    "            안녕하세요, <b>FlowWater</b>입니다.<br>" +
			    "            요청하신 <b>"+ type + "</b>이 아래와 같은 사유로 완료되지 않았습니다." +
			    "        </p>" +
			    "        <div style=\"background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 5px;\">" +
			    "            <p style=\"margin: 0; font-size: 15px; color: #333;\">" +
			    "                <b>실패 사유:</b> <span style=\"color: #e74c3c;\">" + errorMsg + "</span>" +
			    "            </p>" +
			    "        </div>" +
			    "        <div style=\"border-top: 1px solid #DDD; padding: 15px 5px;\">" +
			    "            <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">" +
			    "                도움이 필요하시면 <a href=\"https://www.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다." +
			    "            </p>" +
			    "        </div>" +
			    "    </div>" +
			    "</body>" +
			    "</html>";
		return body;
	}
	
	public String successMessage(String type, Member member) {
		String titleText = "FlowWater 가입을 환영합니다!";
		String mainLink = "https://www.projectwwtp.kro.kr";

		String body = 
		    "<!DOCTYPE html>" +
		    "<html>" +
		    "<head>" +
		    "    <meta charset=\"UTF-8\">" +
		    "    <title>" + titleText + "</title>" +
		    "</head>" +
		    "<body style=\"margin: 0; padding: 0;\">" +
		    "    <div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; border-top: 4px solid #3498db; margin: 50px auto; padding: 30px 0; box-sizing: border-box;\">" +
		    "        <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
		    "            <span style=\"color: #3498db;\">인증 성공!</span> 환영합니다" +
		    "        </h1>" +
		    "        <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
		    "            안녕하세요, <b>" + member.getUserName() + "</b>님!<br>" +
		    "            <b>"+ type + "</b>이 성공적으로 완료되었습니다." +
		    "        </p>";
	    	if(type.equals("이메일 인증")) {		    
	    		body += "        <div style=\"background-color: #f0f8ff; padding: 20px; border-radius: 5px; margin: 20px 5px; border: 1px dashed #3498db;\">" +
			    "            <p style=\"margin: 0; font-size: 15px; color: #333; text-align: center;\">" +
			    "                <b>\"FlowWater와 함께 깨끗하고 스마트한 시작을 함께하세요!\"</b>" +
			    "            </p>" +
			    "        </div>" +
			    "        <p style=\"font-size: 16px; line-height: 26px; padding: 0 5px;\">" +
			    "            아래 버튼을 눌러 메인 화면으로 이동해 보세요." +
			    "        </p>" +
	            "<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px;'> " +
	            "  <tr> " +
	            "    <td align='center' bgcolor='#3498db' width='210' height='45' style='border-radius: 5px; color: #ffffff;'> " +
	            "      <a href='" + mainLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'> " +
	            "        FlowWater 시작하기 " +
	            "      </a> " +
	            "    </td> " +
	            "  </tr> " +
	            "</table>";
	    	}
		    body += "        <div style=\"border-top: 1px solid #DDD; padding: 15px 5px;\">" +
		    "            <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">" +
		    "                도움이 필요하시면 <a href=\"http://wwws.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다." +
		    "            </p>" +
		    "        </div>" +
		    "    </div>" +
		    "</body>" +
		    "</html>";
		return body;
	}
	
	public String reportChart(List<TmsPredict> tmsList, List<FlowPredict> flowList) {
		String titleText = "FlowWater Report";
		LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
		String nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String html = "<!DOCTYPE html>\r\n"
				+ "        <html>\r\n"
				+ "        <head>\r\n"
				+ "            <meta charset=\"UTF-8\">\r\n"
				+ "            <style>\r\n"
				+ "                body { font-family: 'Malgun Gothic', sans-serif; background: #f4f7f9; padding: 20px; color: #333; }\r\n"
				+ "                .container { max-width: 1200px; margin: auto; }\r\n"
				+ "                \r\n"
				+ "                /* 상단 작성 시간 스타일 */\r\n"
				+ "                .timestamp { text-align: right; font-size: 14px; color: #666; margin-bottom: 10px; font-weight: bold; }\r\n"
				+ "                \r\n"
				+ "                .chart-card { background: white; margin-bottom: 25px; padding: 20px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }\r\n"
				+ "                h2 { font-size: 1.1rem; margin-bottom: 15px; border-left: 5px solid #3498db; padding-left: 10px; color: #2c3e50; }\r\n"
				+ "                \r\n"
				+ "                .chart-wrapper { display: flex; position: relative; }\r\n"
				+ "                .y-axis-labels { display: flex; flex-direction: column; justify-content: space-between; width: 50px; margin-right: 10px; text-align: right; font-size: 10px; color: #888; }\r\n"
				+ "                .y-axis-label { height: 20px; line-height: 20px; }\r\n"
				+ "                .chart-container { flex: 1; position: relative; }\r\n"
				+ "                \r\n"
				+ "                .legend { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 10px; padding: 10px; background: #fafafa; border-radius: 6px; }\r\n"
				+ "                .legend-item { display: flex; align-items: center; gap: 5px; font-size: 11px; font-weight: bold; }\r\n"
				+ "                .legend-color { width: 10px; height: 10px; border-radius: 2px; }\r\n"
				+ "                \r\n"
				+ "                svg { width: 100%; height: auto; display: block; }\r\n"
				+ "                .axis { stroke: #ccc; stroke-width: 1; }\r\n"
				+ "                .grid { stroke: #f0f0f0; stroke-width: 1; }\r\n"
				+ "                .line { fill: none; stroke-width: 2.5; stroke-linecap: round; stroke-linejoin: round; }\r\n"
				+ "                .label { font-size: 10px; fill: #888; }\r\n"
				+ "                .point { fill: white; stroke-width: 1.5; cursor: pointer; transition: r 0.2s; }\r\n"
				+ "                .point:hover { r: 5; }\r\n"
				+ "                \r\n"
				+ "                /* 툴팁 스타일 */\r\n"
				+ "                .tooltip {\r\n"
				+ "                    position: absolute;\r\n"
				+ "                    background: rgba(0, 0, 0, 0.85);\r\n"
				+ "                    color: white;\r\n"
				+ "                    padding: 8px 12px;\r\n"
				+ "                    border-radius: 6px;\r\n"
				+ "                    font-size: 12px;\r\n"
				+ "                    pointer-events: none;\r\n"
				+ "                    opacity: 0;\r\n"
				+ "                    transition: opacity 0.2s;\r\n"
				+ "                    z-index: 1000;\r\n"
				+ "                    white-space: nowrap;\r\n"
				+ "                    box-shadow: 0 2px 8px rgba(0,0,0,0.2);\r\n"
				+ "                }\r\n"
				+ "                .tooltip.show {\r\n"
				+ "                    opacity: 1;\r\n"
				+ "                }\r\n"
				+ "                .tooltip-time {\r\n"
				+ "                    font-weight: bold;\r\n"
				+ "                    margin-bottom: 4px;\r\n"
				+ "                    color: #3498db;\r\n"
				+ "                }\r\n"
				+ "                .tooltip-value {\r\n"
				+ "                    margin: 2px 0;\r\n"
				+ "                }\r\n"
				+ "            </style>"
				+ "    			<title>" + titleText + "</title>" + // 브라우저 탭 타이틀
				"        </head>\r\n"
				+ "        <body>\r\n"
				+ "            <div class=\"container\">\r\n"
				+ "                <!-- 작성 시간 표시 영역 -->\r\n"
				+ "                <div class=\"timestamp\" id=\"current-time\">작성 시간 : </div>\r\n"
				+ "\r\n"
				+ "                <!-- 차트 1: 유입유량 -->\r\n"
				+ "                <div class=\"chart-card\">\r\n"
				+ "                    <h2>유입유량 예측</h2>\r\n"
				+ "                    <div class=\"chart-wrapper\">\r\n"
				+ "                        <div class=\"y-axis-labels\" id=\"flow-y-axis\"></div>\r\n"
				+ "                        <div class=\"chart-container\" id=\"chart-flow\" style=\"position: relative;\"></div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "\r\n"
				+ "                <!-- 차트 2-7: TMS 각각의 예측 -->\r\n"
				+ "                <div class=\"chart-card\">\r\n"
				+ "                    <h2>TOC 예측</h2>\r\n"
				+ "                    <div class=\"chart-wrapper\">\r\n"
				+ "                        <div class=\"y-axis-labels\" id=\"toc-y-axis\"></div>\r\n"
				+ "                        <div class=\"chart-container\" id=\"chart-toc\" style=\"position: relative;\"></div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "\r\n"
				+ "                <div class=\"chart-card\">\r\n"
				+ "                    <h2>PH 예측</h2>\r\n"
				+ "                    <div class=\"chart-wrapper\">\r\n"
				+ "                        <div class=\"y-axis-labels\" id=\"ph-y-axis\"></div>\r\n"
				+ "                        <div class=\"chart-container\" id=\"chart-ph\" style=\"position: relative;\"></div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "\r\n"
				+ "                <div class=\"chart-card\">\r\n"
				+ "                    <h2>SS 예측</h2>\r\n"
				+ "                    <div class=\"chart-wrapper\">\r\n"
				+ "                        <div class=\"y-axis-labels\" id=\"ss-y-axis\"></div>\r\n"
				+ "                        <div class=\"chart-container\" id=\"chart-ss\" style=\"position: relative;\"></div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "\r\n"
				+ "                <div class=\"chart-card\">\r\n"
				+ "                    <h2>FLUX 예측</h2>\r\n"
				+ "                    <div class=\"chart-wrapper\">\r\n"
				+ "                        <div class=\"y-axis-labels\" id=\"flus-y-axis\"></div>\r\n"
				+ "                        <div class=\"chart-container\" id=\"chart-flus\" style=\"position: relative;\"></div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "\r\n"
				+ "                <div class=\"chart-card\">\r\n"
				+ "                    <h2>TN 예측</h2>\r\n"
				+ "                    <div class=\"chart-wrapper\">\r\n"
				+ "                        <div class=\"y-axis-labels\" id=\"tn-y-axis\"></div>\r\n"
				+ "                        <div class=\"chart-container\" id=\"chart-tn\" style=\"position: relative;\"></div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "\r\n"
				+ "                <div class=\"chart-card\">\r\n"
				+ "                    <h2>TP 예측</h2>\r\n"
				+ "                    <div class=\"chart-wrapper\">\r\n"
				+ "                        <div class=\"y-axis-labels\" id=\"tp-y-axis\"></div>\r\n"
				+ "                        <div class=\"chart-container\" id=\"chart-tp\" style=\"position: relative;\"></div>\r\n"
				+ "                    </div>\r\n"
				+ "                </div>\r\n"
				+ "            </div>\r\n"
				+ "\r\n"
				+ "            <!-- 툴팁 엘리먼트 -->\r\n"
				+ "            <div class=\"tooltip\" id=\"tooltip\"></div>\r\n"
				+ "\r\n"
				+ "            <script>\r\n"
				+ "                // 현재 시간 표시 함수\r\n"
				+ "                function updateTimestamp() {\r\n"
				+ "                    document.getElementById('current-time').innerText = \"작성 시간 : " + nowStr + "\"\r\n"
				+ "				}\r\n"
				+ "				updateTimestamp();\r\n"
				+ "				\r\n"
				+ "				// 1. 데이터 정의\r\n"
				+ "				const flowData = [\r\n";
		for(int i = 0; i < flowList.size(); ++i) {
			FlowPredict flow = flowList.get(i);
			html += "{ time: \"" + flow.getFlowTime().format(DateTimeFormatter.ofPattern("HH:mm")) + "\", val: " + flow.getFlowValue() + "}";
			if (i < flowList.size() - 1)
				html += ",\r\n";
		}
		html += "                ];\r\n" +
				"\r\n" +
				"                const tmsData = [\r\n";
		for(int i = 0; i < tmsList.size(); ++i) {
			TmsPredict tms = tmsList.get(i);
			html += "{ time: \"" + tms.getTmsTime().format(DateTimeFormatter.ofPattern("HH:mm")) + 
					"\", TOC: " + tms.getToc() + 
					", PH: " + tms.getPh() +
					", SS: " + tms.getSs() +
					", FLUX: " + tms.getFlux() +
					", TN: " + tms.getTn() +
					", TP: " + tms.getTp() + "}";
			if (i < tmsList.size() - 1)
				html += ",\r\n";
		}
		html += "                ];\r\n"
				+ "\r\n"
				+ "                const tmsKeys = [\"TOC\", \"PH\", \"SS\", \"FLUX\", \"TN\", \"TP\"];\r\n"
				+ "                const colors = {\r\n"
				+ "                    TOC: \"#e74c3c\", PH: \"#2ecc71\", SS: \"#f1c40f\", \r\n"
				+ "                    FLUX: \"#9b59b6\", TN: \"#34495e\", TP: \"#e67e22\", flow: \"#3498db\"\r\n"
				+ "                };\r\n"
				+ "\r\n"
				+ "                // 툴팁 엘리먼트\r\n"
				+ "                const tooltip = document.getElementById('tooltip');\r\n"
				+ "\r\n"
				+ "                // 툴팁 표시 함수\r\n"
				+ "                function showTooltip(event, data, key, isSingle) {\r\n"
				+ "                    const value = isSingle ? data.val : data[key];\r\n"
				+ "                    const formattedValue = value.toFixed(2);\r\n"
				+ "                    \r\n"
				+ "                    let content = `<div class=\"tooltip-time\">${data.time}</div>`;\r\n"
				+ "                    if (isSingle) {\r\n"
				+ "                        content += `<div class=\"tooltip-value\">유량: ${formattedValue}</div>`;\r\n"
				+ "                    } else {\r\n"
				+ "                        content += `<div class=\"tooltip-value\">${key}: ${formattedValue}</div>`;\r\n"
				+ "                    }\r\n"
				+ "                    \r\n"
				+ "                    tooltip.innerHTML = content;\r\n"
				+ "                    tooltip.classList.add('show');\r\n"
				+ "                    \r\n"
				+ "                    // 툴팁 위치 설정\r\n"
				+ "                    const x = event.pageX + 10;\r\n"
				+ "                    const y = event.pageY - 10;\r\n"
				+ "                    tooltip.style.left = x + 'px';\r\n"
				+ "                    tooltip.style.top = y + 'px';\r\n"
				+ "                }\r\n"
				+ "\r\n"
				+ "                // 툴팁 숨김 함수\r\n"
				+ "                function hideTooltip() {\r\n"
				+ "                    tooltip.classList.remove('show');\r\n"
				+ "                }\r\n"
				+ "\r\n"
				+ "                function drawChart(targetId, yAxisId, data, keys, isSingle) {\r\n"
				+ "                    const width = 800, height = 300, padding = 40;\r\n"
				+ "                    const chartW = width - (padding * 2);\r\n"
				+ "                    const chartH = height - (padding * 2);\r\n"
				+ "\r\n"
				+ "                    let svg = `<svg viewBox=\"0 0 ${width} ${height}\" xmlns=\"http://www.w3.org/2000/svg\">`;\r\n"
				+ "                    \r\n"
				+ "                    data.forEach((d, i) => {\r\n"
				+ "                        let x = padding + (i * (chartW / (data.length - 1)));\r\n"
				+ "                        svg += `<line x1=\"${x}\" y1=\"${padding}\" x2=\"${x}\" y2=\"${height-padding}\" class=\"grid\" />`;\r\n"
				+ "                        svg += `<text x=\"${x}\" y=\"${height-padding+20}\" class=\"label\" text-anchor=\"middle\">${d.time}</text>`;\r\n"
				+ "                    });\r\n"
				+ "\r\n"
				+ "                    // Y축 레이블 계산 및 렌더링\r\n"
				+ "                    let allValues = [];\r\n"
				+ "                    keys.forEach(key => {\r\n"
				+ "                        const vals = data.map(d => isSingle ? d.val : d[key]);\r\n"
				+ "                        allValues = allValues.concat(vals);\r\n"
				+ "                    });\r\n"
				+ "                    const minVal = Math.min(...allValues);\r\n"
				+ "                    const maxVal = Math.max(...allValues);\r\n"
				+ "                    const range = (maxVal - minVal === 0) ? 1 : (maxVal - minVal);\r\n"
				+ "\r\n"
				+ "                    // 범위에 따라 적절한 단위 동적으로 결정\r\n"
				+ "                    let yAxisUnit;\r\n"
				+ "                    if (range <= 1) {\r\n"
				+ "                        yAxisUnit = 0.1;\r\n"
				+ "                    } else if (range <= 5) {\r\n"
				+ "                        yAxisUnit = 0.5;\r\n"
				+ "                    } else if (range <= 10) {\r\n"
				+ "                        yAxisUnit = 1;\r\n"
				+ "                    } else if (range <= 50) {\r\n"
				+ "                        yAxisUnit = 5;\r\n"
				+ "                    } else if (range <= 100) {\r\n"
				+ "                        yAxisUnit = 10;\r\n"
				+ "                    } else if (range <= 500) {\r\n"
				+ "                        yAxisUnit = 50;\r\n"
				+ "                    } else if (range <= 1000) {\r\n"
				+ "                        yAxisUnit = 100;\r\n"
				+ "                    } else if (range <= 5000) {\r\n"
				+ "                        yAxisUnit = 500;\r\n"
				+ "                    } else if (range <= 10000) {\r\n"
				+ "                        yAxisUnit = 1000;\r\n"
				+ "                    } else if (range <= 50000) {\r\n"
				+ "                        yAxisUnit = 5000;\r\n"
				+ "                    } else {\r\n"
				+ "                        yAxisUnit = 10000;\r\n"
				+ "                    }\r\n"
				+ "\r\n"
				+ "                    // Y축 레이블을 계산된 단위로 표시\r\n"
				+ "                    const yAxisLabels = document.getElementById(yAxisId);\r\n"
				+ "                    yAxisLabels.innerHTML = '';\r\n"
				+ "                    \r\n"
				+ "                    // 최대값을 단위로 올림하여 라벨 범위 결정\r\n"
				+ "                    const maxLabel = Math.ceil(maxVal / yAxisUnit) * yAxisUnit;\r\n"
				+ "                    const minLabel = Math.floor(minVal / yAxisUnit) * yAxisUnit;\r\n"
				+ "                    let labelRange = maxLabel - minLabel;\r\n"
				+ "                    \r\n"
				+ "                    // labelRange이 0이면 최소값으로 설정 (NaN 방지)\r\n"
				+ "                    if (labelRange === 0) {\r\n"
				+ "                        labelRange = yAxisUnit;\r\n"
				+ "                    }\r\n"
				+ "                    \r\n"
				+ "                    // 단위에 따라 라벨 개수 동적으로 결정\r\n"
				+ "                    // 라벨 범위를 단위로 나누고 +1하여 라벨 개수 결정\r\n"
				+ "                    const labelCount = Math.round(labelRange / yAxisUnit) + 1;\r\n"
				+ "                    const labelStep = labelRange / (labelCount - 1);\r\n"
				+ "                    \r\n"
				+ "                    for (let i = 0; i < labelCount; i++) {\r\n"
				+ "                        const val = maxLabel - (labelStep * i);\r\n"
				+ "                        const label = document.createElement('div');\r\n"
				+ "                        label.className = 'y-axis-label';\r\n"
				+ "                        label.style.fontWeight = 'bold';\r\n"
				+ "                        // 소수점 자리 동적 결정\r\n"
				+ "                        const decimals = yAxisUnit < 1 ? Math.abs(Math.floor(Math.log10(yAxisUnit))) : 0;\r\n"
				+ "                        label.textContent = val.toFixed(decimals);\r\n"
				+ "                        yAxisLabels.appendChild(label);\r\n"
				+ "                    }\r\n"
				+ "\r\n"
				+ "                    keys.forEach(key => {\r\n"
				+ "                        const vals = data.map(d => isSingle ? d.val : d[key]);\r\n"
				+ "\r\n"
				+ "                        let points = data.map((d, i) => {\r\n"
				+ "                            let val = isSingle ? d.val : d[key];\r\n"
				+ "                            let x = padding + (i * (chartW / (data.length - 1)));\r\n"
				+ "                            let scaledVal = (val - minLabel) / labelRange * chartH;\r\n"
				+ "                            let y = height - padding - scaledVal;\r\n"
				+ "                            return `${x},${y}`;\r\n"
				+ "                        }).join(\" \");\r\n"
				+ "\r\n"
				+ "                        let strokeColor = isSingle ? colors.flow : colors[key];\r\n"
				+ "                        svg += `<polyline points=\"${points}\" class=\"line\" stroke=\"${strokeColor}\" />`;\r\n"
				+ "                        \r\n"
				+ "                        data.forEach((d, i) => {\r\n"
				+ "                            let val = isSingle ? d.val : d[key];\r\n"
				+ "                            let x = padding + (i * (chartW / (data.length - 1)));\r\n"
				+ "                            let scaledVal = (val - minLabel) / labelRange * chartH;\r\n"
				+ "                            let y = height - padding - scaledVal;\r\n"
				+ "                            svg += `<circle cx=\"${x}\" cy=\"${y}\" r=\"3\" class=\"point\" stroke=\"${strokeColor}\" data-index=\"${i}\" data-key=\"${key}\" />`;\r\n"
				+ "                        });\r\n"
				+ "                    });\r\n"
				+ "\r\n"
				+ "                    svg += `</svg>`;\r\n"
				+ "                    document.getElementById(targetId).innerHTML = svg;\r\n"
				+ "\r\n"
				+ "                    // SVG에 이벤트 리스너 추가\r\n"
				+ "                    const svgElement = document.querySelector(`#${targetId} svg`);\r\n"
				+ "                    svgElement.addEventListener('mouseover', function(e) {\r\n"
				+ "                        if (e.target.classList.contains('point')) {\r\n"
				+ "                            const index = parseInt(e.target.getAttribute('data-index'));\r\n"
				+ "                            const key = e.target.getAttribute('data-key');\r\n"
				+ "                            showTooltip(e, data[index], key, isSingle);\r\n"
				+ "                        }\r\n"
				+ "                    });\r\n"
				+ "\r\n"
				+ "                    svgElement.addEventListener('mouseout', function(e) {\r\n"
				+ "                        if (e.target.classList.contains('point')) {\r\n"
				+ "                            hideTooltip();\r\n"
				+ "                        }\r\n"
				+ "                    });\r\n"
				+ "\r\n"
				+ "                    svgElement.addEventListener('mousemove', function(e) {\r\n"
				+ "                        if (e.target.classList.contains('point')) {\r\n"
				+ "                            const x = e.pageX + 10;\r\n"
				+ "                            const y = e.pageY - 10;\r\n"
				+ "                            tooltip.style.left = x + 'px';\r\n"
				+ "                            tooltip.style.top = y + 'px';\r\n"
				+ "                        }\r\n"
				+ "                    });\r\n"
				+ "                }\r\n"
				+ "\r\n"
				+ "                // 각 차트 그리기 (Y축 단위는 데이터 범위에 따라 자동으로 결정)\r\n"
				+ "                // 범위에 따른 단위: 1이하=0.1, 5이하=0.5, 10이하=1, 50이하=5, 100이하=10, 500이하=50, 1000이하=100, 1000초과=500\r\n"
				+ "                drawChart('chart-flow', 'flow-y-axis', flowData, ['flow'], true);\r\n"
				+ "                drawChart('chart-toc', 'toc-y-axis', tmsData, ['TOC'], false);\r\n"
				+ "                drawChart('chart-ph', 'ph-y-axis', tmsData, ['PH'], false);\r\n"
				+ "                drawChart('chart-ss', 'ss-y-axis', tmsData, ['SS'], false);\r\n"
				+ "                drawChart('chart-flus', 'flus-y-axis', tmsData, ['FLUX'], false);\r\n"
				+ "                drawChart('chart-tn', 'tn-y-axis', tmsData, ['TN'], false);\r\n"
				+ "                drawChart('chart-tp', 'tp-y-axis', tmsData, ['TP'], false);\r\n"
				+ "            </script>\r\n"
			    + "        <div style=\"border-top: 1px solid #DDD; padding: 15px 5px;\">"
			    + "            <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">"
			    + "                도움이 필요하시면 <a href=\"https://www.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다."
			    + "            </p>"
			    + "        </div>"
				+ "        </body>\r\n"
				+ "        </html>";
		return html;
	}
	
	public String reportBody(Member member) {
		String userId = member.getUserId();
		String email = member.getUserEmail();
		String deleteLink = emailAPIDomain + "/api/member/deleteEmail?userId="+userId+"&email="+email;
		String subject = "Report From FlowWater";
		
		String body = "<div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 540px; height: 600px; border-top: 4px solid #3498db; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">" +
	              "    <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
	              "        <span style=\"color: #3498db;\">" + subject + "</span> 안내" +
	              "    </h1>" +
	              "    <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
	              "        첨부된 파일을 다운 받아 12시간 동안의 예측차트를 확인해보세요.<br>" +
	              "    </p>" +
	              "    <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
	              "        더이상 이 보고서를 받지 않으시려면<br>" +
	              "        아래 버튼을 눌러 이메일 정보를 삭제하십시오..<br>" +
	              "    </p>" +
	              "<table cellspacing='0' cellpadding='0' border='0' style='margin: 30px 5px 40px;'> " +
	              "  <tr> " +
	              "    <td align='center' bgcolor='#ff4444' width='210' height='45' style='border-radius: 5px; color: #ffffff;'> " +
	              "      <a href='" + deleteLink + "' target='_blank' style='display: block; width: 210px; height: 45px; font-family: sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; line-height: 45px; text-align: center; font-weight: bold;'> " +
	              "        수신거부 " +
	              "      </a> " +
	              "    </td> " +
	              "  </tr> " +
	              "</table>" +	 	              
	              "    <div style=\"border-top: 1px solid #DDD; padding: 5px;\">" +
				  "        <p style=\"font-size: 12px; line-height: 21px; color: #777; margin: 0;\">" +
				  "            도움이 필요하시면 <a href=\"https://www.projectwwtp.kro.kr/support\" style=\"color: #3498db; text-decoration: none;\">고객지원</a>으로 문의 바랍니다." +
				  "        </p>" +
	              "    </div>" +
	              "</div>";
		return body;
	}

	public Response sendEmail(String email, String subject, String bodyHtml) throws Exception {
        
			Email from = new Email(fromEmail);
			Email to = new Email(email);
			Content content = new Content("text/html", bodyHtml);
			Mail mail = new Mail(from, subject, to, content);
			
			Request request = new Request();
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			
			return sendGrid.api(request);
	}
	
	public void sendEmailList(List<String> addressList, String subjectText, String bodyText) {
        String type = "send All";
		String errorMsg = null;
        try {
        } catch (Exception e) {
			errorMsg = e.getMessage();
			logService.addErrorLog("MailService.java", "sendEmailList()", e.getMessage());
		} finally {
			logService.addMailLog(null, type, errorMsg);	
		}
	}
	
	/**
	 * SVG 차트 이미지를 생성하는 정적 메서드
	 * @param data 데이터 포인트 리스트
	 * @param keys 데이터 키 (TOC, PH, SS 등)
	 * @param isSingle 단일 데이터인지 여부
	 * @param title 차트 제목
	 * @return SVG 문자열
	 */
	private String generateSvgChart(List<? extends Object> data, List<String> keys, boolean isSingle, String title) {
		int width = 1000;
		int height = 350;
		int padding = 60;
		int chartW = width - (padding * 2);
		int chartH = height - (padding * 2);
		
		if (data == null || data.isEmpty()) {
			return "<svg viewBox=\"0 0 " + width + " " + height + "\" xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height + "\">" +
				   "<rect width=\"" + width + "\" height=\"" + height + "\" fill=\"white\"/>" +
				   "<text x=\"500\" y=\"175\" text-anchor=\"middle\" fill=\"#999\">No data</text>" +
				   "</svg>";
		}
		
		// 메모리 효율을 위해 데이터 샘플링 (최대 200개 포인트)
		List<?> sampledData = data;
		int sampleRate = 1;
		int maxDataPoints = 200;
		
		if (data.size() > maxDataPoints) {
			sampleRate = data.size() / maxDataPoints;
			List<Object> temp = new ArrayList<>();
			for (int i = 0; i < data.size(); i += sampleRate) {
				temp.add(data.get(i));
			}
			sampledData = temp;
			System.out.println("[SVG] Data sampled: " + data.size() + " → " + sampledData.size() + " (rate: " + sampleRate + ")");
		}
		
		StringBuilder svg = new StringBuilder();
		// SVG 루트 요소
		svg.append("<svg viewBox=\"0 0 ").append(width).append(" ").append(height)
		   .append("\" xmlns=\"http://www.w3.org/2000/svg\" ")
		   .append("width=\"").append(width).append("\" height=\"").append(height)
		   .append("\" preserveAspectRatio=\"xMidYMid meet\">\n");
		
		// 배경 (흰색, 전체)
		svg.append("<rect x=\"0\" y=\"0\" width=\"").append(width).append("\" height=\"").append(height)
		   .append("\" fill=\"white\"/>\n");
		
		// 제목 추가
		if (title != null && !title.isEmpty()) {
			svg.append("<text x=\"").append(padding).append("\" y=\"25\" font-size=\"16\" font-weight=\"bold\" fill=\"#2c3e50\">")
			   .append(title).append("</text>\n");
		}
		
		// 차트 배경
		svg.append("<rect x=\"").append(padding).append("\" y=\"").append(padding)
		   .append("\" width=\"").append(chartW).append("\" height=\"").append(chartH)
		   .append("\" fill=\"white\" stroke=\"#ddd\" stroke-width=\"1\"/>\n");
		
		// 그리드라인과 X축 레이블 (샘플링된 데이터 기반)
		int dataSize = sampledData.size();
		for (int i = 0; i < dataSize; i++) {
			int x = padding + (int)(i * (double)chartW / (dataSize - 1));
			svg.append("<line x1=\"").append(x).append("\" y1=\"").append(padding)
			   .append("\" x2=\"").append(x).append("\" y2=\"").append(height - padding)
			   .append("\" stroke=\"#f0f0f0\" stroke-width=\"1\"/>\n");
			
			// X축 레이블 (시간)
			String timeLabel = getTimeFromData(sampledData, i);
			svg.append("<text x=\"").append(x).append("\" y=\"").append(height - padding + 25)
			   .append("\" text-anchor=\"middle\" font-size=\"10\" fill=\"#888\">")
			   .append(timeLabel).append("</text>\n");
		}
		
		// Y축 값 범위 계산
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		
		for (Object dataPoint : sampledData) {
			for (String key : keys) {
				double val = getValueFromData(dataPoint, key, isSingle);
				minVal = Math.min(minVal, val);
				maxVal = Math.max(maxVal, val);
			}
		}
		
		// Y축 범위 조정
		double range = (maxVal - minVal == 0) ? 1 : (maxVal - minVal);
		double yAxisUnit = calculateYAxisUnit(range);
		double maxLabel = Math.ceil(maxVal / yAxisUnit) * yAxisUnit;
		double minLabel = Math.floor(minVal / yAxisUnit) * yAxisUnit;
		double labelRange = maxLabel - minLabel;
		if (labelRange == 0) labelRange = yAxisUnit;
		
		// Y축 레이블 (최대 10개로 제한)
		int labelCount = (int)Math.round(labelRange / yAxisUnit) + 1;
		int maxLabels = 10;
		int labelStep = Math.max(1, labelCount / maxLabels);
		
		for (int i = 0; i < labelCount; i += labelStep) {
			double val = maxLabel - (labelRange / (labelCount - 1) * i);
			int y = padding + (int)(i * chartH / (labelCount - 1));
			svg.append("<text x=\"").append(padding - 10).append("\" y=\"").append(y + 5)
			   .append("\" text-anchor=\"end\" font-size=\"10\" fill=\"#888\">")
			   .append(String.format("%.1f", val)).append("</text>\n");
			
			svg.append("<line x1=\"").append(padding).append("\" y1=\"").append(y)
			   .append("\" x2=\"").append(width - padding).append("\" y2=\"").append(y)
			   .append("\" stroke=\"#e0e0e0\" stroke-width=\"1\"/>\n");
		}
		
		// 색상 정의
		HashMap<String, String> colorMap = new HashMap<>();
		colorMap.put("TOC", "#e74c3c");
		colorMap.put("PH", "#2ecc71");
		colorMap.put("SS", "#f1c40f");
		colorMap.put("FLUX", "#9b59b6");
		colorMap.put("TN", "#34495e");
		colorMap.put("TP", "#e67e22");
		colorMap.put("flow", "#3498db");
		
		// 차트 라인 그리기
		for (int keyIdx = 0; keyIdx < keys.size(); keyIdx++) {
			String key = keys.get(keyIdx);
			String color = colorMap.get(key);
			
			// polyline의 points 속성은 "x1,y1 x2,y2 x3,y3" 형식이어야 함
			StringBuilder pointsData = new StringBuilder();
			
			for (int i = 0; i < dataSize; i++) {
				double val = getValueFromData(sampledData.get(i), key, isSingle);
				int x = padding + (int)(i * (double)chartW / (dataSize - 1));
				double scaledVal = (val - minLabel) / labelRange * chartH;
				int y = height - padding - (int)scaledVal;
				
				if (i > 0) {
					pointsData.append(" ");
				}
				pointsData.append(x).append(",").append(y);
			}
			
			svg.append("<polyline points=\"").append(pointsData.toString())
			   .append("\" fill=\"none\" stroke=\"").append(color)
			   .append("\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>\n");
			
			// 데이터 포인트 (모든 포인트는 표시하지 않음, 샘플링된 포인트만)
			for (int i = 0; i < dataSize; i++) {
				double val = getValueFromData(sampledData.get(i), key, isSingle);
				int x = padding + (int)(i * (double)chartW / (dataSize - 1));
				double scaledVal = (val - minLabel) / labelRange * chartH;
				int y = height - padding - (int)scaledVal;
				
				svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
				   .append("\" r=\"3\" fill=\"white\" stroke=\"").append(color)
				   .append("\" stroke-width=\"1.5\"/>\n");
			}
		}
		
		// 범례 추가
		int legendY = padding - 35;
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String color = colorMap.get(key);
			int legendX = padding + (i * 150);
			
			svg.append("<rect x=\"").append(legendX).append("\" y=\"").append(legendY)
			   .append("\" width=\"12\" height=\"12\" fill=\"").append(color).append("\"/>\n");
			svg.append("<text x=\"").append(legendX + 18).append("\" y=\"").append(legendY + 12)
			   .append("\" font-size=\"11\" fill=\"#333\">").append(key).append("</text>\n");
		}
		
		svg.append("</svg>\n");
		return svg.toString();
	}
	
	private double calculateYAxisUnit(double range) {
		if (range <= 1) return 0.1;
		else if (range <= 5) return 0.5;
		else if (range <= 10) return 1;
		else if (range <= 50) return 5;
		else if (range <= 100) return 10;
		else if (range <= 500) return 50;
		else if (range <= 1000) return 100;
		else if (range <= 5000) return 500;
		else if (range <= 10000) return 1000;
		else if (range <= 50000) return 5000;
		else return 10000;
	}
	
	private String getTimeFromData(List<? extends Object> data, int index) {
		Object item = data.get(index);
		try {
			if (item instanceof FlowPredict) {
				return ((FlowPredict)item).getFlowTime().format(DateTimeFormatter.ofPattern("HH:mm"));
			} else if (item instanceof TmsPredict) {
				return ((TmsPredict)item).getTmsTime().format(DateTimeFormatter.ofPattern("HH:mm"));
			}
		} catch (Exception e) {
			logService.addErrorLog("MailService.java", "getTimeFromData()", e.getMessage());
		}
		return "";
	}
	
	private double getValueFromData(Object data, String key, boolean isSingle) {
		try {
			if (isSingle && data instanceof FlowPredict) {
				return ((FlowPredict)data).getFlowValue();
			} else if (data instanceof TmsPredict) {
				TmsPredict tms = (TmsPredict) data;
				switch(key) {
					case "TOC": return tms.getToc();
					case "PH": return tms.getPh();
					case "SS": return tms.getSs();
					case "FLUX": return tms.getFlux();
					case "TN": return tms.getTn();
					case "TP": return tms.getTp();
				}
			}
		} catch (Exception e) {
			logService.addErrorLog("MailService.java", "getValueFromData()", e.getMessage());
		}
		return 0;
	}
	
	/**
	 * SVG를 PNG 바이트 배열로 변환 (CID용)
	 * @param svgString SVG 문자열
	 * @return PNG 이미지의 바이트 배열
	 */
	private byte[] convertSvgToPngBytes(String svgString) {
		try {
			System.out.println("[SVG Conversion] Starting PNG conversion...");
			System.out.println("[SVG Conversion] Original SVG Size: " + svgString.length() + " bytes");
			System.out.println("[SVG Conversion] SVG Content (first 200 chars): " + svgString.substring(0, Math.min(200, svgString.length())));
			
			// SVG가 완전한 문서 구조를 가져야 함
			String completeSvg = svgString.trim();
			
			// XML 선언 추가
			if (!completeSvg.startsWith("<?xml")) {
				completeSvg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + completeSvg;
			}
			
			// DOCTYPE 추가
			if (!completeSvg.contains("<!DOCTYPE")) {
				completeSvg = completeSvg.replace("<svg", 
					"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n<svg");
			}
			
			System.out.println("[SVG Conversion] Complete SVG Size: " + completeSvg.length() + " bytes");
			
			// Batik 트랜스코더 설정
			PNGTranscoder transcoder = new PNGTranscoder();
			transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 1000f);
			transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 350f);
			
			// SVG 입력 생성
			ByteArrayInputStream svgInput = new ByteArrayInputStream(completeSvg.getBytes(StandardCharsets.UTF_8));
			
			// PNG 출력 생성
			ByteArrayOutputStream pngOutput = new ByteArrayOutputStream();
			
			// 트랜스코더 입출력 설정
			TranscoderInput input = new TranscoderInput(svgInput);
			TranscoderOutput output = new TranscoderOutput(pngOutput);
			
			System.out.println("[SVG Conversion] Executing transcode...");
			
			// 트랜스코딩 실행
			transcoder.transcode(input, output);
			
			byte[] pngBytes = pngOutput.toByteArray();
			System.out.println("[SVG Conversion] Success! PNG Size: " + pngBytes.length + " bytes");
			
			return pngBytes;
			
		} catch (org.apache.batik.transcoder.TranscoderException te) {
			System.err.println("[SVG Conversion] Batik TranscoderException:");
			System.err.println("  Message: " + te.getMessage());
			if (te.getCause() != null) {
				System.err.println("  Cause: " + te.getCause().getMessage());
				te.getCause().printStackTrace();
			}
			te.printStackTrace();
			
			String errorMsg = "Batik TranscoderException: " + te.getMessage();
			if (te.getCause() != null) {
				errorMsg += " (Cause: " + te.getCause().getMessage() + ")";
			}
			logService.addErrorLog("MailService.java", "convertSvgToPngBytes()", errorMsg);
			return new byte[0];
			
		} catch (Exception e) {
			System.err.println("[SVG Conversion] Error converting SVG to PNG:");
			System.err.println("  Exception Type: " + e.getClass().getName());
			System.err.println("  Message: " + e.getMessage());
			e.printStackTrace();
			
			String errorMsg = "SVG to PNG conversion failed: " + e.getClass().getSimpleName() + " - " + e.getMessage();
			logService.addErrorLog("MailService.java", "convertSvgToPngBytes()", errorMsg);
			return new byte[0];
		}
	}
	
	/**
	 * CID를 사용하여 차트 이미지를 메일에 포함시키는 메서드
	 * @param member 수신자
	 * @param subject 메일 제목
	 * @param bodyHtml 메일 본문
	 * @param tmsList TMS 데이터
	 * @param flowList 유입유량 데이터
	 */
	public void sendEmailWithChartAsCID(Member member, String subject, String bodyHtml, 
										List<TmsPredict> tmsList, List<FlowPredict> flowList,
										String fileName, String chart) {
		String type = "sendReport";
		String errorMsg = null;
        try {
        	// 고정된 타임스탬프 생성 (모든 CID에서 동일하게 사용)
        	String timestamp = String.valueOf(System.currentTimeMillis());
        	
        	// 메일 본문 생성 (CID 참조 포함)
        	String bodyWithCidImages = generateChartBodyWithCID(bodyHtml, tmsList, flowList, timestamp);
        	
        	Email from = new Email(fromEmail);
			Email to = new Email(member.getUserEmail());
			Content content = new Content("text/html", bodyWithCidImages);
			Mail mail = new Mail(from, subject, to, content);
			
			// 유입유량 차트
			String flowSvg = generateSvgChart(flowList, java.util.Arrays.asList("flow"), true, "");
			byte[] flowPng = convertSvgToPngBytes(flowSvg);
			if (flowPng.length > 0) {
				Attachments flowAttachment = new Attachments();
				flowAttachment.setContent(Base64.getEncoder().encodeToString(flowPng));
				flowAttachment.setFilename("chart_flow.png");
				flowAttachment.setType("image/png");
				flowAttachment.setDisposition("inline");
				flowAttachment.setContentId("chart_flow_" + timestamp);
				mail.addAttachments(flowAttachment);
				System.out.println("[Mail CID] Added flow chart with CID: chart_flow_" + timestamp);
			}
			
			// TMS 차트들
			String[] tmsKeys = {"TOC", "PH", "SS", "FLUX", "TN", "TP"};
			for (String key : tmsKeys) {
				String tmsSvg = generateSvgChart(tmsList, java.util.Arrays.asList(key), false, "");
				byte[] tmsPng = convertSvgToPngBytes(tmsSvg);
				if (tmsPng.length > 0) {
					Attachments tmsAttachment = new Attachments();
					tmsAttachment.setContent(Base64.getEncoder().encodeToString(tmsPng));
					tmsAttachment.setFilename("chart_" + key.toLowerCase() + ".png");
					tmsAttachment.setType("image/png");
					tmsAttachment.setDisposition("inline");
					tmsAttachment.setContentId("chart_" + key + "_" + timestamp);
					mail.addAttachments(tmsAttachment);
					System.out.println("[Mail CID] Added " + key + " chart with CID: chart_" + key + "_" + timestamp);
				}
			}
			
			Request request = new Request();
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			
			Response response = sendGrid.api(request);
			System.out.println("sendEmail response : " + response.getStatusCode());
			if(response.getStatusCode() != 202)
				errorMsg = response.getBody();
        } catch (Exception e) {
			errorMsg = e.getMessage();
			logService.addErrorLog("MailService.java", "sendEmailWithChartAsCID()", e.getMessage());
		} finally {
			logService.addMailLog(member, type, errorMsg);	
		}
	}
	
	/**
	 * CID 참조가 포함된 메일 본문 생성
	 * @param bodyHtml 기본 메일 본문
	 * @param tmsList TMS 데이터
	 * @param flowList 유입유량 데이터
	 * @param timestamp CID에 사용할 타임스탬프
	 * @return CID 참조가 포함된 HTML
	 */
	private String generateChartBodyWithCID(String bodyHtml, List<TmsPredict> tmsList, List<FlowPredict> flowList, String timestamp) {
		StringBuilder html = new StringBuilder(bodyHtml);
		
		// CSS 스타일
		html.append("<div style=\"font-family: Arial, sans-serif; background: #f9f9f9; padding: 20px;\">\n");
		html.append("<div style=\"max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px;\">\n");
		
		// 유입유량 차트
		html.append("<div style=\"margin-bottom: 30px;\">\n");
		html.append("<h3 style=\"color: #2c3e50; border-left: 4px solid #3498db; padding-left: 10px; margin: 0 0 15px 0;\">유입유량 예측</h3>\n");
		html.append("<img src=\"cid:chart_flow_").append(timestamp).append("\" style=\"width: 100%; height: auto; border: 1px solid #ddd; border-radius: 4px;\" alt=\"유입유량 차트\" />\n");
		html.append("</div>\n");
		
		// TMS 차트들
		String[] tmsKeys = {"TOC", "PH", "SS", "FLUX", "TN", "TP"};
		for (String key : tmsKeys) {
			html.append("<div style=\"margin-bottom: 30px;\">\n");
			html.append("<h3 style=\"color: #2c3e50; border-left: 4px solid #3498db; padding-left: 10px; margin: 0 0 15px 0;\">")
			    .append(key).append(" 예측</h3>\n");
			html.append("<img src=\"cid:chart_").append(key).append("_").append(timestamp).append("\" style=\"width: 100%; height: auto; border: 1px solid #ddd; border-radius: 4px;\" alt=\"").append(key).append(" 차트\" />\n");
			html.append("</div>\n");
		}
		
		html.append("</div>\n");
		html.append("</div>\n");
		
		return html.toString();
	}
}

