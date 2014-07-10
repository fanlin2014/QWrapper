import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.travelco.rdf.infocenter.InfoCenter;

/**
 * 返程
 * @author corder
 * Codebase：	gjsairok001
		测试搜索条件：	PRG-SGN 2014-08-23 2014-08-29
					BKK-AMS 2014-08-24 2014-8-30
					BCN-PRG 2014-08-31 2014-09-05
 */

public class Wrapper_gjsairok001  implements QunarCrawler{

	public static void main(String args[]){
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("PRG");
		searchParam.setArr("BCN");
		searchParam.setDepDate("2014-08-01");
		searchParam.setRetDate("2014-08-08");
		searchParam.setTimeOut("600000");
		searchParam.setWrapperid("gjsairok001");
		searchParam.setToken("");
		
		Wrapper_gjsairok001 t = new Wrapper_gjsairok001();
		// 得到请求返回的html网页
		String html = t.getHtml(searchParam);
		System.out.println("********************得到了请求返回的网页"+html);
		// 将网页的结果进行封装
		t.process(html, searchParam);
	}
	
	// 双程
	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		// 模拟点击按钮时候的第一次请求的url
		String bookingUrlPre = "http://secure.csa.cz/en/ibs/";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("get");
		Map<String, String> map = new LinkedHashMap<String, String>();		
		//处理下需要处理的参数--------------------------------------------待补充
		String[] depArry = arg0.getDepDate().split("-");
		String[] retArray = arg0.getRetDate().split("-");   //回返日期
	
		//处理出发地点，到达地点  
		String dep0 = getCityFromCode(arg0.getDep());//出发地点  只会输入三字节码，需要转换
		String arr0 = getCityFromCode(arg0.getArr());//到达地点  只会输入三字节码，需要转换
		map.put("next","1");
		map.put("cabinPreference","");
		map.put("password","1");
		map.put("PRICER_PREF","FRP");
		map.put("AIRLINES","ok");
		map.put("ID_LOCATION","CZ");
		map.put("JOURNEY_TYPE","RT");	//双程类型不一致
		map.put("DEP_0",dep0);
		map.put("ARR_0",arr0);
		map.put("DEP_1","");	
		map.put("ARR_1","");	
		map.put("DAY_0",Integer.valueOf(depArry[2]).toString());	//日期赋值出发往返
		map.put("MONTH_SEL_0",Integer.valueOf(depArry[1])+"/"+depArry[0]);
		map.put("DAY_1",Integer.valueOf(retArray[2]).toString());
		map.put("MONTH_SEL_1",Integer.valueOf(retArray[1])+"/"+retArray[0]);
		map.put ("ADTCOUNT","1");
		map.put ("CHDCOUNT","0");
		map.put ("INFCOUNT","0");
		bookingInfo.setInputs(map);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}
	
	public String getHtml(FlightSearchParam arg0) {
		QFGetMethod get = null;
		//定义最终返回的html的内容
		String contentFinal = "";
		try {	
			// 生成httpClient对象
			QFHttpClient httpClient = new QFHttpClient(arg0, false);
			// 按照浏览器的模式来处理cookie
			httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
	
			// 处理日期，拼装参数时用到
			String[] dateArry = arg0.getDepDate().trim().split("-");
			String[] retArry = arg0.getRetDate().trim().split("-");
			int day = Integer.valueOf(dateArry[2]);
			int month= Integer.valueOf(dateArry[1]);
			String year = dateArry[0];
			
			int day2 = Integer.valueOf(retArry[2]);
			int month2 = Integer.valueOf(retArry[1]);
			String year2 = retArry[0];
			
			// 如果有特殊的字符，需要对特殊字符进行编码
			String month_select = java.net.URLEncoder.encode(month + "/" +year,"UTF-8");
			String month_select1 = java.net.URLEncoder.encode(month2 + "/" +year2,"UTF-8");
			
			String dep0 = getCityFromCode(arg0.getDep());//出发地点  只会输入三字节码，需要转换
			String arr0 = getCityFromCode(arg0.getArr());//到达地点  只会输入三字节码，需要转换  
			dep0 = java.net.URLEncoder.encode(dep0,"UTF-8");
			arr0 = java.net.URLEncoder.encode(arr0,"UTF-8");
			
			String url = "http://secure.csa.cz/en/ibs/?next=1&cabinPreference=&password=1&PRICER_PREF=FRP&AIRLINES=ok&ID_LOCATION=CZ&" +
					"JOURNEY_TYPE=RT&DEP_0="+dep0+"&ARR_0="+arr0+"&DEP_1=&ARR_1=&" +
					"DAY_0="+day+"&MONTH_SEL_0="+month_select+"&DAY_1="+day2+"&MONTH_SEL_1="+month_select1+"&ADTCOUNT=1&CHDCOUNT=0&INFCOUNT=0";
			//实际请求的url地址
//			url="http://secure.csa.cz/en/ibs/?next=1&cabinPreference=&password=1&PRICER_PREF=FRP&AIRLINES=ok&ID_LOCATION=CZ&" +
//					"JOURNEY_TYPE=RT&DEP_0=Prague%2B%28PRG%29&ARR_0=Ho%2BChi%2BMinh%2BCity%2B%28SGN%29&DEP_1=&ARR_1=&" +
//					"DAY_0=23&MONTH_SEL_0=8%2F2014&DAY_1=29&MONTH_SEL_1=8%2F2014&ADTCOUNT=1&CHDCOUNT=0&INFCOUNT=0";
			// 进行的是get请求，创建get方法的实例
			get = new QFGetMethod(url);
			get.getParams().setContentCharset("utf-8");
			httpClient.executeMethod(get);
			// 执行第一次请求返回的页面信息
			String content1 = get.getResponseBodyAsString();

//			// 获取第二次请求的urlURL
			int a = content1.indexOf("URL=");
			int b = content1.indexOf("</head>");
			String url2 = content1.substring(a+4, b-2).toString();
			String url2sid = url2.substring(url2.indexOf("?")+1, url2.length());;
			get = new QFGetMethod(url2);
			get.getParams().setContentCharset("utf-8");
			get.setRequestHeader("Referer", url);
			httpClient.executeMethod(get);
		
			// 进行第三次请求
			String url3 = "http://secure.csa.cz/en/ibs/ajaxSectorCalendarOffer.php?sector=0&fareOfferData=0&"+url2sid;
			url3 = "http://secure.csa.cz/en/ibs/ajaxSectorCalendarOffer.php?sector=0&fareOfferData=0&userFareOfferData=0&"+url2sid;
			
			get = new QFGetMethod(url3);
			get.getParams().setContentCharset("utf-8");
			get.setRequestHeader("Referer", url2);
			httpClient.executeMethod(get);		
			String content3 = get.getResponseBodyAsString();
			
			// 根据第三次请求返回的内容获取到 参数 fareId0的具体值
			int start3 = content3.lastIndexOf(arg0.getDepDate());     //("2014-08-23");  
			String content3Temp = content3.substring(0, start3 + arg0.getDepDate().trim().length());
			String fareId0 = content3Temp.substring(content3Temp.lastIndexOf("value=")+7, content3Temp.length());
			System.out.println("fareId==========给到第四次请求的参数"+fareId0);
			fareId0 = java.net.URLEncoder.encode(fareId0, "UTF-8");//对特殊字符进行编码
			
			//进行第4次请求
			String url4 = "http://secure.csa.cz/en/ibs/ajaxSectorCalendarOffer.php?sector=1&"+url2sid+"&fareOfferData="+fareId0+"&userFareOfferData="+fareId0;
			get = new QFGetMethod(url4); 
			get.getParams().setContentCharset("utf-8");
			get.setRequestHeader("Referer", url2);
			httpClient.executeMethod(get);
			String content4 = get.getResponseBodyAsString();
			//得到下次请求fareId1的具体值
			
			int start4 = content4.lastIndexOf(arg0.getRetDate());   // ("2014-08-29");
			String content4Temp = content4.substring(0, start4 + arg0.getRetDate().trim().length());
			String fareId1 = content4Temp.substring(content4Temp.lastIndexOf("value=")+7, content4Temp.length());
			fareId1 = java.net.URLEncoder.encode(fareId1, "UTF-8");//对特殊字符进行编码
			
			// 进行第5次请求获取到结果 
			String url5 = "http://secure.csa.cz/en/ibs/ajaxSectorItineraryOffer.php?sector=0&"+url2sid;		
			// 进行第5次请求的url拼装
			url5 = url5+"&fareId0="+fareId0+"&fareId1="+fareId1;
			get = new QFGetMethod(url5); 
			get.getParams().setContentCharset("utf-8");
			get.setRequestHeader("Referer", url2);
			httpClient.executeMethod(get);
			String content5 = get.getResponseBodyAsString();
			int start5 = content5.lastIndexOf("\" checked=\"checked\"");
			String content5Temp = content5.substring(0, start5);
			String itineraryId0 = content5Temp.substring(content5Temp.lastIndexOf("value=")+7,content5Temp.length());
			itineraryId0 = java.net.URLEncoder.encode(itineraryId0, "UTF-8");
			
			//进行第6次请求，得到最终的结果的html网页，在上次请求多封装了个参数
			String url6 = "http://secure.csa.cz/en/ibs/ajaxSectorItineraryOffer.php?sector=1&"+url2sid +"&fareId0="+fareId0+"&fareId1="+fareId1+ "&itineraryId0="+itineraryId0;
			get = new QFGetMethod(url6); 
			get.setRequestHeader("Referer", url2);
			get.getParams().setContentCharset("utf-8");
			httpClient.executeMethod(get);
		
			contentFinal = get.getResponseBodyAsString(); // 得到的最终返回的网页
			System.out.println("返回的价格的相关的详细信息====="+contentFinal);
			return contentFinal;
		} catch (Exception e) {			
			e.printStackTrace();
		} finally{
			if (null != get){
				get.releaseConnection();
			}
		}
		return "Exception";
	}
	
	public ProcessResultInfo process(String html, FlightSearchParam fightSearchParam) {
		System.out.println("**************************处理价格信息，封装最终结果");
		/* ProcessResultInfo中，
		 * ret为true时，status可以为：SUCCESS(抓取到机票价格)|NO_RESULT(无结果，没有可卖的机票)
		 * ret为false时，status可以为:CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL|PARAM_ERROR
		 */
		ProcessResultInfo result = new ProcessResultInfo();
		if ("Exception".equals(html)) {	
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;			
		}		
		//需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		//if (html.contains("We are sorry, the departure flight does not operate or has no availability for the date")) {
		if(html.contains("There are no available flights to your destination. Please choose another departure date.")){
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;			
		}

		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String[] price = parsePrice(html);
			if (price == null || price.length != 3) {
				throw new Exception("parse price fail");
			}
			List<String[]> flightNo = parseFightNo(html);
			if (flightNo == null || flightNo.size() == 0) {
				throw new Exception("parse flight no fail");
			}
			String depYear = StringUtils.substringBefore(fightSearchParam.getDepDate(), "-");
			String[] dateTime = parseDateTime(html, depYear);  
			//if (dateTime == null || dateTime.length != 8) {
			if (dateTime == null || dateTime.length == 0) {
				throw new Exception("parse date time fail");
			}
			// 获取去程、返程的相关的信息
			List<String[]> outIt = parseItinerary(html, "out");
			List<String[]> retIt = parseItinerary(html, "ret");
		
			RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
			FlightDetail flightDetail = new FlightDetail();

			flightDetail.setDepcity(fightSearchParam.getDep());
			flightDetail.setArrcity(fightSearchParam.getArr());
			flightDetail.setDepdate(format.parse(fightSearchParam.getDepDate()));
			flightDetail.setWrapperid(fightSearchParam.getWrapperid());
			
			flightDetail.setMonetaryunit(price[0].trim().toUpperCase());
			flightDetail.setTax(Double.parseDouble(price[2]));//总的税费，包括燃油附加费、小费、服务费    // 获取的只有总的税费
			flightDetail.setPrice(Double.parseDouble(price[1]));   // 获取的只有总的价钱
			List<String> flightNoList = new ArrayList<String>();
			for (String q : flightNo.get(0)) {
				flightNoList.add(q);
			}
			flightDetail.setFlightno(flightNoList);

			List<FlightSegement> segs = new ArrayList<FlightSegement>();
			for (int i = 0; i < flightNo.get(0).length; ++i) {
				//FlightSegement outSeg = new FlightSegement(flightNo.get(0)[i]);
				FlightSegement outSeg = new FlightSegement();
				if (i == 0) {
					outSeg.setDepDate(dateTime[0]);
					outSeg.setDeptime(dateTime[1]);
					outSeg.setArrDate(dateTime[2]);
					outSeg.setArrtime(dateTime[3]);
					outSeg.setDepairport(outIt.get(i)[0].replace("(", "").replace(")", ""));
					outSeg.setArrairport(outIt.get(i)[1].replace("(", "").replace(")", ""));
					
				} else if (i == flightNo.get(0).length - 1){
					outSeg.setDepDate(dateTime[4]);
					outSeg.setDeptime(dateTime[5]);
					outSeg.setArrDate(dateTime[6]);
					outSeg.setArrtime(dateTime[7]);
					outSeg.setDepairport(outIt.get(i)[0].replace("(", "").replace(")", ""));
					outSeg.setArrairport(outIt.get(i)[1].replace("(", "").replace(")", ""));
				}
				outSeg.setFlightno(flightNo.get(0)[i]);
				segs.add(outSeg);
			}
			baseFlight.setInfo(segs);

			segs = new ArrayList<FlightSegement>(); 
			System.out.println(flightNo.get(1).length);
			for (int i = 0; i < flightNo.get(1).length; ++i) {
				//FlightSegement retSeg = new FlightSegement(flightNo.get(1)[i]);
				FlightSegement retSeg = new FlightSegement();
				if(flightNo.get(1).length == 1){
					retSeg.setDepDate(dateTime[4]);
					retSeg.setDeptime(dateTime[5]);
					retSeg.setArrDate(dateTime[6]);
					retSeg.setArrtime(dateTime[7]);	
					retSeg.setDepairport(fightSearchParam.getArr());
					retSeg.setArrairport(retIt.get(i)[1].replace("(", "").replace(")", ""));
				}else{
					if (i == 0) {
						retSeg.setDepDate(dateTime[8]);
						retSeg.setDeptime(dateTime[9]);
						retSeg.setArrDate(dateTime[10]);
						retSeg.setArrtime(dateTime[11]);	
						retSeg.setDepairport(fightSearchParam.getArr());
						retSeg.setArrairport(retIt.get(i)[1].replace("(", "").replace(")", ""));
					} else if (i == flightNo.get(1).length - 1) {
						retSeg.setDepDate(dateTime[12]);
						retSeg.setDeptime(dateTime[13]);
						retSeg.setArrDate(dateTime[14]);
						retSeg.setArrtime(dateTime[15]);	
						retSeg.setDepairport(retIt.get(i)[0].replace("(", "").replace(")", ""));
						retSeg.setArrairport(retIt.get(i)[1].replace("(", "").replace(")", ""));
					}
				}
				retSeg.setFlightno(flightNo.get(1)[i]);
				segs.add(retSeg);
			}

			baseFlight.setRetinfo(segs);
			baseFlight.setDetail(flightDetail);
			//baseFlight.setOutboundPrice(Double.parseDouble(price[1]));    //设置去程的总价钱 (获取的只有总价钱)
			baseFlight.setRetdepdate(format.parse(fightSearchParam.getRetDate()));
			flightNoList = new ArrayList<String>();
			for (String q : flightNo.get(1)) {
				flightNoList.add(q);
			}
			baseFlight.setRetflightno(flightNoList);
			//baseFlight.setReturnedPrice(Double.parseDouble(price[2]));  //设置回来的价钱（获取的只有总的价钱）

			List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
			flightList.add(baseFlight);
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			result.setData(flightList);
			System.out.println("******************************************************返回了最终的结果result");
			return result;
		} catch(Exception e){
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			return result;
		}
	}
	
	// 获取返程的价格的信息
	public String[] parsePrice(String html){
		String[] price = null;
		String htmlTemp =  StringUtils.substringBetween(html,"<h4>Prices for all passengers incl. taxes and fees</h4>", "</description2>");
		//获取到价钱的table
		String baseAmount = StringUtils.substringBetween(htmlTemp, "<span id=\"baseAmount_ADULT\">","</span>").replace(String.valueOf((char)160), "");
		baseAmount = baseAmount.replace("?", "");
		if(baseAmount == null){
			return null;
		}else{
			price = new String[3];
		}
		String diff_currency = StringUtils.substringBetween(htmlTemp, "<span class=\"diff_currency\">","</span>");
		String fuelSurcharge = StringUtils.substringBetween(htmlTemp, "<span id=\"fuelSurcharge_ADULT\">","</span>").replace(String.valueOf((char)160), "");
		String taxAmount = StringUtils.substringBetween(htmlTemp, "<span id=\"taxAmount_ADULT\">","</span>").replace(String.valueOf((char)160), "");
		String serviceAmount = StringUtils.substringBetween(htmlTemp, "<span id=\"serviceAmount_ADULT\">","</span>").replace(String.valueOf((char)160), "");
		//String totalAmount = StringUtils.substringBetween(htmlTemp,"<span id=\"totalAmount_ADULT\">","</span>").replace("?", "");
		
		// 看是否需要，进行取舍
		fuelSurcharge = fuelSurcharge.replace("?", "");
		taxAmount = taxAmount.replace("?", "");
		serviceAmount = serviceAmount.replace("?", "");
		
		// 计算总的税费
		double taxall = Double.valueOf(fuelSurcharge.substring(0, fuelSurcharge.length()-3)) + 
						Double.valueOf(taxAmount.substring(0, taxAmount.length()-3))+
						Double.valueOf(serviceAmount.substring(0, serviceAmount.length()-3));
		price[0] = diff_currency;	//单位
		price[1] = baseAmount.substring(0, baseAmount.length()-3);		//基本费用
		price[2] = String.valueOf(taxall);	// 总税费
		return price;
	}
	
	/**
	 * 解析航班号
	 * @param html
	 * @return String[] 0: 去程，1：返程
	 */
	private List<String[]> parseFightNo(String html){
		System.out.println("html==="+html);
		//获取内容有效的部分
		String htmlTemp = StringUtils.substringBetween(html, "<h4>Your selected flights</h4>", "</div>") ;
		htmlTemp = StringUtils.substringBetween(htmlTemp, "<tbody", "</tbody>") ;
		//得到了存储信息的数组
		String arryCf[]  = StringUtils.substringsBetween(htmlTemp, "<tr class=\"first_row\">", "</tr>");
		String arryFh[]  = StringUtils.substringsBetween(htmlTemp, "<tr class=\"next_row\">", "</tr>");
		
		List<String[]> result = new ArrayList<String[]>();
		
		String[] tdArry = new String[]{};
		String[] td1Arry = new String[]{}; 
		String flightnoFirst = "";
		String flightnonext = "";
	
		//firstrow
		for(int i = 0;i<arryCf.length;i++){
			//得到所有的td
			tdArry = StringUtils.substringsBetween(arryCf[i],"<td>","</td>");
			String flightNo = tdArry[tdArry.length-2].replaceAll(String.valueOf((char)160), ""); //处理航班号空格
			flightnoFirst +=  flightNo +",";
		}
		//next_row
		if(arryFh != null){
			for(int i = 0;i<arryFh.length;i++){
				//得到所有的td
				td1Arry = StringUtils.substringsBetween(arryFh[i],"<td>","</td>");
				String flightNo = td1Arry[td1Arry.length-2].replaceAll(String.valueOf((char)160), "");
				flightnonext += flightNo + ","; 
			}
		}
		// 拼装去的航班号信息，返回的航班号信息
		for(int i=0;i<arryCf.length;i++){
			if(!"".equals(flightnonext)){
				String temp = (flightnoFirst.split(",")[i]+","+flightnonext.split(",")[i]).replace("?", " ");
				result.add(temp.split(","));
			}else{
				String temp = flightnoFirst.split(",")[i];
				result.add(new String[]{temp});
			}
		}
		return result;
	}
	
	/**
	 * 解析时间 
	 * @param html
	 * @param depYear
	 * @return  0: 去程出发日期
	 * 			1: 去程出发时间
	 * 			2: 去程到达日期
	 *          3: 去程到达时间  去1 
	 *          
	 *          4: 去程出发日期
	 * 			5: 去程出发时间
	 * 			6: 去程到达日期
	 *          7: 去程到达时间  去2 
	 *          
	 *          8: 回程出发日期
	 *          9: 回程出发时间
	 *          10: 回程到达日期
	 *          11: 回程到达时间 回1
	 *          
	 *          12: 回程出发日期
	 *          13: 回程出发时间
	 *          14: 回程到达日期
	 *          15: 回程到达时间 回2
	 */
	private String[] parseDateTime(String html, String depYear) {
		String[] result = new String[16];
		try{
			int idx = 0;
			//获取内容有效的部分
			String htmlTemp = StringUtils.substringBetween(html, "<h4>Your selected flights</h4>", "</div>") ;
			htmlTemp = StringUtils.substringBetween(htmlTemp, "<tbody", "</tbody>") ;
			String[] trArry = StringUtils.substringsBetween(htmlTemp, "<tr", "</tr>");
			System.out.println(trArry.length);
			//去程1
			String[] qc = StringUtils.substringsBetween(trArry[0],"<strong>","</strong>");
			String[] qc1 = qc[0].split(" ");
			result[idx++] = formateDate(qc1[0]);    //2014/Aug/23
			result[idx++] = qc1[1];    
			String[] qc2 = qc[1].split(" ");
			result[idx++] = formateDate(qc2[0]);    
			result[idx++] = qc2[1];

			//去程2 (回1)
			String[] qcOther = StringUtils.substringsBetween(trArry[1],"<strong>","</strong>");
			String[] qcOther1 = qcOther[0].split(" ");
			result[idx++] = formateDate(qcOther1[0]);    //2014/Aug/23
			result[idx++] = qcOther1[1];    
			String[] qcOther2 = qcOther[1].split(" ");
			result[idx++] = formateDate(qcOther2[0]);    
			result[idx++] = qcOther2[1];
			
			System.out.println("**************************************获取去程时间信息");
			if(trArry.length > 2){
				//回程1
				String[] hc = StringUtils.substringsBetween(trArry[2],"<strong>","</strong>");
				String[] hc1 = hc[0].split(" ");
				result[idx++] = formateDate(hc1[0]);
				result[idx++] = hc1[1];
				String[] hc2 = hc[1].split(" ");
				result[idx++] = formateDate(hc2[0]);
				result[idx++] = hc2[1];
				
				//回程2
				String[] hcOther = StringUtils.substringsBetween(trArry[3],"<strong>","</strong>");
				String[] hcOther1 = hcOther[0].split(" ");
				result[idx++] = formateDate(hcOther1[0]);
				result[idx++] = hcOther1[1];
				String[] hcOther2 = hcOther[1].split(" ");
				result[idx++] = formateDate(hcOther2[0]);
				result[idx++] = hcOther2[1];
				System.out.println("**************************************获取回程的时间信息");
			}
			return result;
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	// 英文转月份
	private String month2No(String month) {
		month = month.toLowerCase();
		if (month.equals("jan")) {
			return "01";
		} else if (month.equals("feb")) {
			return "02";
		} else if (month.equals("mar")) {
			return "03";
		} else if (month.equals("apr")) {
			return "04";
		} else if (month.equals("may")) {
			return "05";
		} else if (month.equals("jun")) {
			return "06";
		} else if (month.equals("jul")) {
			return "07";
		} else if (month.equals("aug")) {
			return "08";
		} else if (month.equals("sep")) {
			return "09";
		} else if (month.equals("oct")) {
			return "10";
		} else if (month.equals("nov")) {
			return "11";
		} else if (month.equals("dec")) {
			return "12";
		}
		return null;
	}
	
	private List<String[]> parseItinerary(String html, String type) {
		String xml = StringUtils.substringBetween(html, "<h4>Your selected flights</h4>", "</div>") ;
		xml = StringUtils.substringBetween(xml, "<tbody", "</tbody>");
		String[] trArry = StringUtils.substringsBetween(xml, "<tr", "</tr>");
		List<String[]> list = new ArrayList<String[]>();
		int bj = 0;
		// 设置数组取的下标
		if(trArry.length == 2){
			if(type == "out"){
				String[] arry1 = new String[2];
				// 去程 1 去程2  回程1 回程2  （集合中有四个对象）
				String tdArry0[] = StringUtils.substringsBetween(trArry[bj],"<td>","</td>");
				String qc1 = tdArry0[0].substring(0, tdArry0[0].indexOf("<br/>"));
				String[] qc1Arry = qc1.split(" ");
				arry1[0] = qc1Arry[qc1Arry.length - 1];
				
				String qc2 = tdArry0[1].substring(0, tdArry0[1].indexOf("<br/>"));
				String[] qc2Arry = qc2.split(" ");
				arry1[1] = qc2Arry[qc2Arry.length - 1];
				list.add(arry1);
			}else{
				String[] arry2 = new String[2];
				String tdArry1[] = StringUtils.substringsBetween(trArry[bj+1],"<td>","</td>");
			
				String qc3 = tdArry1[0].substring(0, tdArry1[0].indexOf("<br/>"));
				String[] qc3Arry = qc3.trim().split(" ");
				arry2[0] = qc3Arry[qc3Arry.length - 1];
				
				String qc4 = tdArry1[1].substring(0, tdArry1[1].indexOf("<br/>"));
				String[] qc4Arry = qc4.trim().split(" ");
				arry2[1] = qc4Arry[qc4Arry.length - 1];
				System.out.println("********************************************************获取航班号信息");
				list.add(arry2);
			}
		}else{
			if(type == "out"){
				bj = 0;
			}else{
				bj = 2;
			}
			String[] arry1 = new String[2];
			// 去程 1 去程2  回程1 回程2  （集合中有四个对象）
			String tdArry0[] = StringUtils.substringsBetween(trArry[bj],"<td>","</td>");
			String qc1 = tdArry0[0].substring(0, tdArry0[0].indexOf("<br/>"));
			String[] qc1Arry = qc1.split(" ");
			arry1[0] = qc1Arry[qc1Arry.length - 1];
			
			String qc2 = tdArry0[1].substring(0, tdArry0[1].indexOf("<br/>"));
			String[] qc2Arry = qc2.split(" ");
			arry1[1] = qc2Arry[qc2Arry.length - 1];
			
			// 去程 2
			String[] arry2 = new String[2];
			String tdArry1[] = StringUtils.substringsBetween(trArry[bj+1],"<td>","</td>");
		
			String qc3 = tdArry1[0].substring(0, tdArry1[0].indexOf("<br/>"));
			String[] qc3Arry = qc3.trim().split(" ");
			arry2[0] = qc3Arry[qc3Arry.length - 1];
			
			String qc4 = tdArry1[1].substring(0, tdArry1[1].indexOf("<br/>"));
			String[] qc4Arry = qc4.trim().split(" ");
			arry2[1] = qc4Arry[qc4Arry.length - 1];
			list.add(arry1);
			list.add(arry2);
		}
		return list;
	}
	//得到城市名称
	private String getCityFromCode(String code){
		String cityName = InfoCenter.transformCityName(InfoCenter.getCityFromCityCode(code, null), "en").toLowerCase();
		cityName = cityName.replace(" ", "+") + "+(" + code +")";
		return cityName;
	}
	// 处理日期字符串  date = "2014/Aug/23";  得到 2014-08-23
	private String formateDate(String date){
		String[] dateArry= date.split("/");
		String month = month2No(dateArry[1]);
		return dateArry[0]+"-"+month+"-"+dateArry[2];
	}
}
