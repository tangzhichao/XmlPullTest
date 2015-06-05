package util.xmlpull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import collection.map.LinkedHashMap;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class XmlUtils {

	/**
	 * 解析XML
	 * 
	 * @param parser
	 * @return
	 */
	public static LinkedHashMap<String, Object> parserXml(XmlPullParser parser) {
		return parserXml(parser, "\n");
	}

	/**
	 * 解析XML
	 * 
	 * @param inputStream
	 * @return
	 */
	public static LinkedHashMap<String, Object> parserXml(InputStream inputStream) {
		return parserXml(inputStream, "UTF-8");
	}

	/**
	 * 解析XML
	 * 
	 * @param inputStream
	 * @param encoding
	 * @return
	 */
	public static LinkedHashMap<String, Object> parserXml(InputStream inputStream, String encoding) {
		return parserXml(inputStream, encoding, "\n");
	}

	/**
	 * 解析XML
	 * 
	 * @param reader
	 * @return
	 */
	public static LinkedHashMap<String, Object> parserXml(Reader reader) {
		return parserXml(reader, "\n");
	}

	/**
	 * 解析XML
	 * 
	 * @param inputStream
	 * @param encoding
	 * @param mergeTextStr
	 * @return
	 */
	public static LinkedHashMap<String, Object> parserXml(InputStream inputStream, String encoding, String mergeTextStr) {
		XmlPullParser parser = null;
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(inputStream, encoding);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return parserXml(parser, mergeTextStr);
	}

	/**
	 * 解析XML
	 * 
	 * @param reader
	 * @param mergeTextStr
	 * @return
	 */
	public static LinkedHashMap<String, Object> parserXml(Reader reader, String mergeTextStr) {
		XmlPullParser parser = null;
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return parserXml(parser, mergeTextStr);
	}

	/**
	 * 解析XML
	 * 
	 * @param parser
	 * @param mergeTextStr
	 * @return
	 */
	public static LinkedHashMap<String, Object> parserXml(XmlPullParser parser, String mergeTextStr) {

		Map<Integer, LinkedHashMap<String, Object>> depthList = new HashMap<>();
		LinkedHashMap<String, Object> root = new LinkedHashMap<>();
		try {
			int eventType = parser.getEventType();
			for (; eventType != XmlPullParser.END_DOCUMENT;) {

				int tagDepth = parser.getDepth();
				if (eventType == XmlPullParser.START_TAG) {

					String tagName = parser.getName();

					// 获取当前层标签的map
					LinkedHashMap<String, Object> currDepthMap = (LinkedHashMap<String, Object>) depthList.get(tagDepth);
					if (currDepthMap == null) {
						currDepthMap = new LinkedHashMap<>();
						depthList.put(tagDepth, currDepthMap);
					}
					LinkedHashMap<String, Object> currElementMap = (LinkedHashMap<String, Object>) currDepthMap.get(tagName);
					if (currElementMap == null) {
						currElementMap = new LinkedHashMap<>();
						currDepthMap.put(tagName, currElementMap);
					}

					// 获取上一层标签的map
					if (tagDepth > 1) {
						LinkedHashMap<String, Object> parentDepthMap = (LinkedHashMap<String, Object>) depthList.get(tagDepth - 1);
						// 找到父标签
						LinkedHashMap<String, Object> parentElementMap = (LinkedHashMap<String, Object>) parentDepthMap.getLast();
						// 添加到父标签中，建立包含关系
						parentElementMap.put(tagName, currElementMap);
					} else if (tagDepth == 1) {// 如果是根元素
						root.put(tagName, currElementMap);
					}

					// 添加属性
					int attributeCount = parser.getAttributeCount();
					for (int i = 0; i < attributeCount; i++) {
						String attributeName = parser.getAttributeName(i);
						String attributeValue = parser.getAttributeValue(i);
						currElementMap.put(attributeName, attributeValue);
					}
				} else if (eventType == XmlPullParser.TEXT) {

					String text = parser.getText();
					if (tagDepth >= 1) {

						LinkedHashMap<String, Object> currDepthMap = (LinkedHashMap<String, Object>) depthList.get(tagDepth);
						Entry<String, LinkedHashMap<String, Object>> currElementEntry = (Entry) currDepthMap.getLastEntry();

						if (text != null && !text.trim().isEmpty()) {
							Object object = currElementEntry.getValue().get(currElementEntry.getKey());
							if (object != null) {
								currElementEntry.getValue().put(currElementEntry.getKey(), object.toString() + mergeTextStr + text);
							} else {
								currElementEntry.getValue().put(currElementEntry.getKey(), text);
							}
						}
					}
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return root;
	}

	public static void writeXml(LinkedHashMap<String, Object> map, OutputStream outputStream, String encoding) {
		try (OutputStream out = outputStream) {
			XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
			serializer.setOutput(out, encoding);

			serializer.startDocument(encoding, true);

			Entry<String, Object> firstEntry = map.getFirstEntry();

			addElement(serializer, null, firstEntry);

			serializer.endDocument();
			serializer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addElement(XmlSerializer serializer, Entry<String, Object> parent, Entry<String, Object> entry) throws Exception {
		String key = entry.getKey();
		Object value = entry.getValue();
		if (value instanceof LinkedHashMap) {
			serializer.startTag(null, key);
			LinkedHashMap map = (LinkedHashMap) value;
			for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Object> child = (Entry<String, Object>) iterator.next();
				addElement(serializer, entry, child);
			}
			serializer.endTag(null, key);
		} else {
			if (parent != null) {
				String parentKey = parent.getKey();
				if (parentKey == null || (parentKey.intern() != key)) {
					serializer.attribute(null, key, (String) value);
				} else {
					serializer.text((String) value);
				}
			} else {
				try {
					serializer.attribute(null, key, (String) value);
				} catch (Exception e) {
					serializer.text((String) value);
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		FileInputStream inputStream = new FileInputStream("person.xml");
		LinkedHashMap<String, Object> map = XmlUtils.parserXml(inputStream, "UTF-8", "\n");
		System.out.println(map);
		LinkedHashMap<String, Object> persons = (LinkedHashMap<String, Object>) map.get("persons");
		String personsText = (String) persons.get("persons");
		System.out.println("'" + personsText + "'");

		writeXml(map, new FileOutputStream("test.xml"), "UTF-8");

	}

}
