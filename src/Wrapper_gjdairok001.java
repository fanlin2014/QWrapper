import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.travelco.rdf.infocenter.InfoCenter;

/**
 * 单程数据方法
 * @author corder
 * @date  2014-6-16
 * Codebase：	gjdairok001
   测试搜索条件：	PRG-SGN 2014-08-23
				BKK-AMS 2014-08-24
				BCN-PRG 2014-08-31
 */
public class Wrapper_gjdairok001 implements QunarCrawler{
	
	// 订票的功能完成
	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		// 模拟点击按钮时候的第一次请求的url
		String bookingUrlPre = "http://secure.csa.cz/en/ibs/";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("get");
		Map<String, String> map = new LinkedHashMap<String, String>();		
		//处理下需要处理的参数--------------------------------------------待补充
		String date = arg0.getDepDate();
		String[] dateArry = date.split("-");
		
		//处理出发地点，到达地点  
		String dep0 = arg0.getDep();//出发地点  只会输入三字节码，需要转换（测试后不需要转换）
		String arr0 = arg0.getArr();//到达地点  只会输入三字节码，需要转换（测试后不需要转换）
		
		map.put("next","1");
		map.put("cabinPreference","");
		map.put("password","1");
		map.put("PRICER_PREF	FRP","");
		map.put("AIRLINES","ok");
		map.put("ID_LOCATION	CZ","");
		map.put("JOURNEY_TYPE","OW");
		map.put("DEP_0",dep0);
		map.put("ARR_0",arr0);
		map.put("DEP_1","");	
		map.put("ARR_1","");	
		map.put("DAY_0",String.valueOf(Integer.valueOf(dateArry[2])));
		map.put("MONTH_SEL_0",Integer.valueOf(dateArry[1])+"/"+dateArry[0]);
		map.put("DAY_1",String.valueOf(Integer.valueOf(dateArry[2])));
		map.put("MONTH_SEL_1",Integer.valueOf(dateArry[1])+"/"+dateArry[0]);
		map.put ("ADTCOUNT","1");
		map.put ("CHDCOUNT","0");
		map.put ("INFCOUNT","0");
		bookingInfo.setInputs(map);
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	// 获取请求后返回的html的相关的信息
	public String getHtml(FlightSearchParam arg0) {
		//sQFGetMethod get = null;
		//定义最终返回的html的内容
		String contentFinal = "";
		try {	
			// 生成httpClient对象
			QFHttpClient httpClient = new QFHttpClient(arg0, false);
			// 按照浏览器的模式来处理cookie
			httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		
			String[] dateArry = arg0.getDepDate().trim().split("-");
			int day = Integer.valueOf(dateArry[2]);
			int month= Integer.valueOf(dateArry[1]);
			String year = dateArry[0];
			// 如果有特殊的字符，需要对特殊字符进行编码
			String month_select = java.net.URLEncoder.encode(month + "/" +year,"UTF-8");
	
			String dep0 = getCityFromCode(arg0.getDep());//出发地点  只会输入三字节码，需要转换
			String arr0 = getCityFromCode(arg0.getArr());//到达地点  只会输入三字节码，需要转换
			//String dep0 = arg0.getDep();//出发地点  
			//String arr0 = arg0.getArr();//到达地点  
			dep0 = java.net.URLEncoder.encode(dep0,"UTF-8");
			arr0 = java.net.URLEncoder.encode(arr0,"UTF-8");
			
			String url = "http://secure.csa.cz/en/ibs/?next=1&cabinPreference=&password=1&PRICER_PREF=FRP&" +
				"AIRLINES=ok&ID_LOCATION=CZ&JOURNEY_TYPE=OW&DEP_1=&ARR_1=" +
				"&DEP_0="+dep0+"&ARR_0="+ arr0 +"&DAY_0=" +day+"&MONTH_SEL_0="+month_select +
				"&DAY_1="+day+"&MONTH_SEL_1="+month_select+"&ADTCOUNT=1&CHDCOUNT=0&INFCOUNT=0";
            System.out.println(url+"===");
            //url = "http://secure.csa.cz/en/ibs/?next=1&cabinPreference=&password=1&PRICER_PREF=FRP&AIRLINES=ok&ID_LOCATION=CZ&JOURNEY_TYPE=OW&DEP_0=Prague%2B%28PRG%29&ARR_0=Ho%2BChi%2BMinh%2BCity%2B%28SGN%29&DEP_1=&ARR_1=&DAY_0=23&MONTH_SEL_0=8%2F2014&DAY_1=23&MONTH_SEL_1=8%2F2014&ADTCOUNT=1&CHDCOUNT=0&INFCOUNT=0";
				
            // 进行的是get请求，创建get方法的实例
			QFGetMethod get = new QFGetMethod(url);
			get.getParams().setContentCharset("utf-8");
			httpClient.executeMethod(get);
			// 执行第一次请求返回的页面信息
			String content1 = get.getResponseBodyAsString();
			
			int a = content1.indexOf("URL=");
			int b = content1.indexOf("</head>");
			String url2 = content1.substring(a+4, b-2).toString();
			String url2sid = url2.substring(url2.indexOf("?")+1, url2.length());
			get = new QFGetMethod(url2);
			get.getParams().setContentCharset("utf-8");
			get.setRequestHeader("Referer", url);
			httpClient.executeMethod(get);
			
			String url3 = "http://secure.csa.cz/en/ibs/ajaxSectorCalendarOffer.php?sector=0&fareOfferData=0&"+url2sid;
			get = new QFGetMethod(url3);
			get.getParams().setContentCharset("utf-8");
			get.setRequestHeader("Referer", url2);
			httpClient.executeMethod(get);			
			String content3 = get.getResponseBodyAsString();
			
			// 根据第三次请求返回的内容获取到 参数 fareId0的具体值
			int start = content3.indexOf(arg0.getDepDate());
			String content3Temp = content3.substring(0, start+arg0.getDepDate().trim().length());
			String fareId0 = content3Temp.substring(content3Temp.lastIndexOf("value=")+7, content3Temp.length());
			
			// 进行第四次请求获取到结果
			String url4 = "http://secure.csa.cz/en/ibs/ajaxSectorItineraryOffer.php?sector=0&"+url2sid;			 
			fareId0 = java.net.URLEncoder.encode(fareId0, "utf-8");			
			// 进行第四次请求的url拼装
			url4 = url4+"&fareId0="+fareId0;
			get = new QFGetMethod(url4); 
			get.getParams().setContentCharset("utf-8");
			get.setRequestHeader("Referer", url2);
			httpClient.executeMethod(get);
			contentFinal = get.getResponseBodyAsString();
			get.releaseConnection();
			return contentFinal;
		} catch (Exception e) {			
			e.printStackTrace();
		} finally{
		}
		
		return "Exception";
	}

	//解析数据，返回最终的封装结果的方法（======================================================================待修改）
	public ProcessResultInfo process(String html, FlightSearchParam fightSearchParam) {
		// TODO Auto-generated method stub
		ProcessResultInfo result = new ProcessResultInfo();
		if ("Exception".equals(html)) {	
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;			
		}		
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		//if (html.contains("We are sorry, the departure flight does not operate or has no availability for the date")) {
		if(html.contains("There are no available flights to your destination. Please choose another departure date.")){
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;			
		}
		// 解析html，封装成指定形式的结果
		try{
			// 日期格式化
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			
			// 封装flightDetail 集合信息
			FlightDetail flightDetail = new FlightDetail();
			
			// 获取价格的数组信息
			String[] price = parsePrice(html);
			if (price == null || price.length != 3) {
				throw new Exception("parse price fail");
			}
			flightDetail.setMonetaryunit(price[0].trim().toUpperCase()); //钱的单位
			flightDetail.setPrice(Double.parseDouble(price[1])); // 机票的基本价格
			flightDetail.setTax(Double.parseDouble(price[2]));	// 机票的税费
			
			
			// 获取航班号的数组信息
			String[] flightNo = parseFightNo(html);
			// 获取 parseItinerary 集合信息
			List<String[]> iter = parseItinerary(html);
		    
			// 如果航班号的个数!= 详细信息的集合的长度，获取的有问题。
			if (iter.size() != flightNo.length) {
				throw new Exception("parse fligh no or itinerary fail");
			}
				
			// 将航班号的数组封装为String对象集合
			ArrayList<String> flightNolist = new ArrayList<String>();
			for (String q : flightNo) {
				flightNolist.add(q);
			}
	
			flightDetail.setFlightno(flightNolist);	
			flightDetail.setDepcity(fightSearchParam.getDep());
			flightDetail.setArrcity(fightSearchParam.getArr());
			flightDetail.setDepdate(format.parse(fightSearchParam.getDepDate()));// 日期待处理================
			flightDetail.setWrapperid(fightSearchParam.getWrapperid());
			
			//封装 FlightSegement 集合信息
			List<FlightSegement> segs = new ArrayList<FlightSegement>();
			for(int i =0 ;i<iter.size();i++){
				FlightSegement seg = new FlightSegement();
				seg.setFlightno(iter.get(i)[6]); //航班号
				seg.setArrDate(formateDate(iter.get(i)[1]));//出发日期 2014/Aug/23 需格式化
				seg.setArrtime(iter.get(i)[2]); //出发时间 14:10
				String arrPort = iter.get(i)[0];
				seg.setArrairport(arrPort);//出发机场 (PRG) 需要得到机场信息
				
				seg.setDepDate(formateDate(iter.get(i)[4])); //到达日期	
				seg.setDeptime(iter.get(i)[5]); 	//到达时间 
				String depPort = iter.get(i)[3];
				seg.setDepairport(depPort);//到达机场 （ICN）需要得到机场信息
				//将封装好的对象增加到集合中
				segs.add(seg);
			}
			
			// 将信息封装到result中
			OneWayFlightInfo baseFlight = new OneWayFlightInfo();
			baseFlight.setDetail(flightDetail);
			baseFlight.setInfo(segs);
			
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			flightList.add(baseFlight);
			result.setData(flightList);
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			System.out.println("返回处理的结果！！===============================");
			return result;
		}catch(Exception e){
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			return result;		
		}
	}
	
	// 价格
	public String[] parsePrice(String html){
		String[] price = null;
		try{
		
		//直接获取价格的信息
		String baseAmount = StringUtils.substringBetween(html, "<span id=\"baseAmount_ADULT\">","</span>").replaceAll("[?]", "");
		if(baseAmount == null){
			return null;
		}else{
			price = new String[3];
		}
		String diff_currency = StringUtils.substringBetween(html, "<span class=\"diff_currency\">","</span>");
		int lengthCurr =  diff_currency.length();
		String fuelSurcharge = StringUtils.substringBetween(html, "<span id=\"fuelSurcharge_ADULT\">","</span>");
		String taxAmount = StringUtils.substringBetween(html, "<span id=\"taxAmount_ADULT\">","</span>");
		String serviceAmount = StringUtils.substringBetween(html, "<span id=\"serviceAmount_ADULT\">","</span>");
		//String totalAmount = StringUtils.substringBetween(html,"<span id=\"totalAmount_ADULT\">","</span>").replace("?", "");
		
		//税费、服务费、燃油费
		baseAmount = baseAmount.toString().substring(0, baseAmount.length()-lengthCurr).replaceAll(String.valueOf((char)160), "");
		taxAmount = taxAmount.substring(0, taxAmount.length()-lengthCurr).replaceAll(String.valueOf((char)160), "");
		fuelSurcharge = fuelSurcharge.substring(0, fuelSurcharge.length() - lengthCurr).replaceAll(String.valueOf((char)160), "");
		serviceAmount = serviceAmount.substring(0, serviceAmount.length()-lengthCurr ).replaceAll(String.valueOf((char)160), "");
		
		double taxAll =  Double.valueOf(taxAmount)+Double.valueOf(fuelSurcharge)+Double.valueOf(serviceAmount);
		//包含了货币单位、基本价格、税费（包含了税费、服务费、燃油费）
		price = new String[]{diff_currency,baseAmount,String.valueOf(taxAll)};
		return price;
		}catch(Exception e){
			e.printStackTrace();
		}
		return price;
	}
	
	private String getCityFromCode(String code){
		String cityName = InfoCenter.transformCityName(InfoCenter.getCityFromCityCode(code, null), "en").toLowerCase();
		cityName = cityName.replace(" ", "+") + "+(" + code +")";
		return cityName;
	}
	public String formatPrice(String price){
		String priceTemp = "";
		String tags = "[,\\.\\s;!?]+";
		priceTemp = price.replaceAll("[\\s\"]", "");
		priceTemp = price.replaceAll(tags, "");
		return priceTemp;
	}
	
	// 航班号
	public String[] parseFightNo(String html){
		// 获取的html，得到选中的航班的详细信息的table
		String tempHtml = StringUtils.substringBetween(html,"<h4>Your selected flights</h4>","</div>");
		Map map = this.readStringXmlOut(tempHtml);
		String flightNo = (String)map.get("flightNo");
		String[] parseFightNo = flightNo.substring(0, flightNo.length()-1).split(",");
		return  parseFightNo;
	}
	
	// 得到每个航班号对应的信息
	public List<String[]> parseItinerary(String html){
		// 获取的html，得到选中的航班的详细信息的table
		String tempHtml = StringUtils.substringBetween(html,"<h4>Your selected flights</h4>","</div>");
		Map map = this.readStringXmlOut(tempHtml);
		List<String[]> list = (List<String[]>)map.get("flightlist");
		return list;
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
	
	// 处理日期字符串  date = "2014/Aug/23";  得到 2014-08-23
	private String formateDate(String date){
		String[] dateArry= date.split("/");
		String month = month2No(dateArry[1]);
		return dateArry[0]+"-"+month+"-"+dateArry[2];
	}
	
	// 解析xml，封装为map类型的数据
	public static Map readStringXmlOut(String xml) {
			Map map = new HashMap();
			String flightNo = "";
			List<String[]> list = new ArrayList<String[]>();
			
			String[] contentArry = StringUtils.substringsBetween(xml, "<tbody", "</tbody>");
			if(contentArry.length > 0){
				String info[] = null;
				for(String str : contentArry){
					//不包含对其进行处理
					if(str.indexOf("style=\"display:none;\"") < 0){
						// 得到第一个航班号 firstrow
						String trContent = StringUtils.substringBetween(str, "<tr class=\"first_row\">", "</tr>");
						if(trContent != null && !"".equals(trContent)){
							//System.out.println(trContent);
							String[] trContentArr = StringUtils.substringsBetween(trContent, "<td", "</td>");
							info = new String[10];
							for(int i=0 ;i<trContentArr.length;i++){
								trContentArr[i] = trContentArr[i].substring(1, trContentArr[i].length());
							}
							String td1 = trContentArr[0].substring(1, trContentArr[0].length());
							String td2 = trContentArr[1].substring(1, trContentArr[1].length());
							
							String td1city = td1.substring(0, td1.indexOf("<br/>")).trim();
							String td1Date = StringUtils.substringBetween(td1, "<strong>", "</strong>");
							String cityName = StringUtils.substringBetween(td1city, "(", ")");
							info[0] = cityName; //城市三字码
							info[1] = td1Date.trim().split(" ")[0]; //出发日期
							info[2] = td1Date.trim().split(" ")[1]; //出发时间
							
							String td2city = td2.substring(0, td2.indexOf("<br/>")).trim();
							String td2Date = StringUtils.substringBetween(td2, "<strong>", "</strong>");
							String cityName2 = StringUtils.substringBetween(td2city, "(", ")");
							info[3] = cityName2;					//到达城市
							info[4] = td2Date.trim().split(" ")[0]; //到达日期
							info[5] = td2Date.trim().split(" ")[1]; //到达日期
							info[6] = trContentArr[5].replace("?", " "); //航班号信息
						
							list.add(info);
							flightNo += trContentArr[5].replace("?", " ") + ",";
						}
						
						// 得到第二个航班号 nextrow
						String trnContent = StringUtils.substringBetween(str, "<tr class=\"next_row\">","</tr>");
						if(trnContent != null && !"".equals(trnContent)){
							//System.out.println(trnContent);
							String[] trnContentArr = StringUtils.substringsBetween(trnContent, "<td", "</td>");
							info = new String[10];
							for(int i=0 ;i<trnContentArr.length;i++){
								trnContentArr[i] = trnContentArr[i].substring(1, trnContentArr[i].length());
							}
							String td1 = trnContentArr[0].substring(1, trnContentArr[0].length());
							String td2 = trnContentArr[1].substring(1, trnContentArr[1].length());
							
							String td1city = td1.substring(0, td1.indexOf("<br/>")).trim();
							String td1Date = StringUtils.substringBetween(td1, "<strong>", "</strong>");
							String cityName = StringUtils.substringBetween(td1city, "(", ")");
							info[0] = cityName; //城市三字码
							info[1] = td1Date.trim().split(" ")[0]; //出发日期
							info[2] = td1Date.trim().split(" ")[1]; //出发时间
							
							String td2city = td2.substring(0, td2.indexOf("<br/>")).trim();
							String td2Date = StringUtils.substringBetween(td2, "<strong>", "</strong>");
							String cityName2 = StringUtils.substringBetween(td2city, "(", ")");
							info[3] = cityName2;					//到达城市
							info[4] = td2Date.trim().split(" ")[0]; //到达日期
							info[5] = td2Date.trim().split(" ")[1]; //到达日期
							info[6] = trnContentArr[2].replace("?", " "); //航班号信息
			
							list.add(info);
							flightNo += trnContentArr[2].replace("?", " ") + ",";
						}
					}
				}
				//将查询的值封装到map集合中
				map.put("flightNo", flightNo);
				map.put("flightlist", list);
			}
			return map;
	}
	
	// 测试得到的数据封装后的最终的结果
	public static void main(String[] args){
		// 封装查询的结果 BKK-AMS 2014-08-24
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("BKK");
		searchParam.setArr("AMS");
		searchParam.setDepDate("2014-08-24");
		searchParam.setTimeOut("600000"); //设置超时时间
		searchParam.setWrapperid("gjdairok001");
		searchParam.setToken("");
		
		// 根据请求地址、请求条件获取到页面的信息
		Wrapper_gjdairok001 jkhk = new  Wrapper_gjdairok001();
		String html = jkhk.getHtml(searchParam);
		
		// 处理页面的数据
		ProcessResultInfo result = new ProcessResultInfo();
		result = jkhk.process(html,searchParam);
	}
}
