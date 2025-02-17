/* ZKClientTestCase.java

{{IS_NOTE
	Purpose:

	Description:

	History:
		Dec 4, 2009 9:50:12 AM , Created by jumperchen
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.ztl;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.selenium.SeleniumException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.touch.TouchActions;

import org.zkoss.ztl.unit.*;
import org.zkoss.ztl.util.ConfigHelper;
import org.zkoss.ztl.util.Scripts;
import org.zkoss.ztl.util.image.Comparator;

/**
 * A skeleton of ZK client widget.
 * @author jumperchen
 *
 */
public class ZKClientTestCase extends ZKTestCase {
	protected int _timeout;

	/**
	 * Waits for Ajax response. (includes animation check by default)
	 * <p>By default the timeout time is specified in config.properties
	 * @see #waitResponse(int)
	 */
	protected void waitResponse() {
		waitResponse(_timeout, true);
	}

	/**
	 * Waits for Ajax response.
	 * <p>By default the timeout time is specified in config.properties
	 * @param includingAnimation if true, it will include animation check.
	 * @see #waitResponse(int, boolean)
	 * @since 2.0.0
	 */
	protected void waitResponse(boolean includingAnimation) {
		waitResponse(_timeout, includingAnimation);
	}

	/**
	 * Verifies the image before response has done. 
	 */
	@Override
	public void verifyImage(Comparator comparator) {
		waitResponse();
		super.verifyImage(comparator);
	}

	/**
	 * Waits for Ajax response according to the timeout attribute.
	 * @param timeout
	 * @param includingAnimation if true, it will include animation check.
	 * @since 2.0.0
	 * 
	 */
	protected void waitResponse(int timeout, boolean includingAnimation) {
		long s = System.currentTimeMillis();
		int i = 0;
		int ms = Integer.parseInt(getSpeed());
		if (isIE())
			ms /= 2;

		String scripts = includingAnimation ? "!!zAu.processing() || !!jq.timers.length" : "!!zAu.processing()";
		while (i < 2) { // make sure the command is triggered.
			while (Boolean.valueOf(this.getEval(scripts))) {
				if (System.currentTimeMillis() - s > timeout) {
					assertTrue("Test case timeout!", false);
					break;
				}
				i = 0;//reset
				sleep(ms);
			}
			i++;
			sleep(includingAnimation ? ms * 2 : ms);
		}
	}

	/**
	 * Waits for Ajax response according to the timeout attribute.(excluding animation check)
	 * @param timeout the time. (millisecond).
	 * @see #waitResponse(int, boolean)
	 */
	protected void waitResponse(int timeout) {
		waitResponse(timeout, false);
	}

	/**
	 * Remove all of the children of the given widget.(internal use only)
	 */
	protected void removeChildren(Widget w) {
		Widget f = w.firstChild();
		while (f.exists()) {
			f.detach();
		}
	}

	protected void runZscript(String zscript) {
		getEval("zAu.send(new zk.Event(null, 'onZTLService', '" + zscript + "', 10))");
	}

	protected void runRawZscript(String zscript) {
		runZscript(zscript.trim().replace("\\", "\\\\").replace("'", "\\'").replaceAll("\r", "").replaceAll("\n",
				"\\\\n"));
	}

	/**
	 * Verify image after waitResponse(). (including animation time)
	 * @since 2.0.0
	 */
	public void verifyImage() {
		waitResponse(true);
		super.verifyImage();
	}

	/**
	 * Returns the Widget object of the UUID.
	 * @param number the number of the widget ID.
	 * @see #uuid(int)
	 * @see #widget(String)
	 */
	protected Widget widget(int number) {
		return widget(uuid(number));
	}

	/**
	 * Returns the Widget object of the UUID.
	 * @param uuid the widget ID.
	 */
	protected Widget widget(String uuid) {
		return new Widget(uuid);
	}

	/**
	 * Returns the Widget object from the JQuery object.
	 * @param jQuery the JQuery object.
	 */
	protected Widget widget(JQuery jQuery) {
		return new Widget(jQuery);
	}

	/**
	 * Returns the Jquery object of the selector
	 * <p> Default: without "#" sign
	 * @param selector the selector
	 */
	protected JQuery jq(String selector) {
		return new JQuery(selector);
	}

	/**
	 * Returns the Jquery object of the ZKClientObject.
	 * @param el the ZKClientObject
	 */
	protected JQuery jq(ClientWidget el) {
		return new JQuery(el);
	}

	/**
	 * Returns the ZK object of the ZKClientObject.
	 * @param el the ZKClientObject
	 */
	protected ZK zk(ClientWidget el) {
		return new ZK(el);
	}

	/**
	 * Returns the ZK object of the selector
	 * @param selector the selector of the element
	 */
	protected ZK zk(String selector) {
		return new ZK(selector);
	}

	public void check(ClientWidget locator) {
		super.check(locator.toLocator());
	}

	public void click(ClientWidget locator) {
		if (!isSafari())
			super.click(locator.toLocator());
		else
			clickAt(locator, "2,2");
	}

	/**
	 * Clicks the element at the offset point.
	 *
	 * @param locator element
	 * @param coordString x, y offset. Start from top-left.
	 */
	public void clickAt(ClientWidget locator, String coordString) {
		try {
			// workaround for https://github.com/SeleniumHQ/selenium/issues/4847
			if (isFirefox()) {
				String[] froms = coordString.split(",");
				int x0 = Integer.parseInt(froms[0].trim());
				int y0 = Integer.parseInt(froms[1].trim());
				Dimension dim = findElement(locator).getSize();
				int x1 = x0 - dim.width / 2;
				int y1 = y0 - dim.height / 2;
				coordString = String.format("%d,%d", x1, y1);
			}
			super.clickAt(locator.toLocator(), coordString);
		} catch (SeleniumException e) {
			// Opera seems not to support clickAt() fixed for B30-2562880.ztl
			// Firefox3 for B30-1903399.ztl
			try {
				Scripts.triggerMouseEventAt(getWebDriver(), locator, "mousedown", coordString);
				Scripts.triggerMouseEventAt(getWebDriver(), locator, "mouseup", coordString);
				Scripts.triggerMouseEventAt(getWebDriver(), locator, "click", coordString);
			} catch (SeleniumException ee) {
				throw e;
			}
		}
	}

	public void contextMenu(ClientWidget locator) {
		getActions().moveToElement(findElement(locator)).contextClick().perform();
//		Scripts.triggerMouseEventAt(getWebDriver(), locator, "contextmenu", "5,5");
	}

	public void contextMenuAt(ClientWidget locator, String coordString) {
		String[] coord = coordString.split(",");
		getActions().moveToElement(findElement(locator), Integer.parseInt(coord[0]), Integer.parseInt(coord[1])).contextClick().perform();
//		Scripts.triggerMouseEventAt(getWebDriver(), locator, "contextmenu", coordString);
	}

	public void doubleClick(ClientWidget locator) {
		// don't use doubleClick(), because it fails in IE
		doubleClickAt(locator, "1,1");
	}

	public void doubleClickAt(ClientWidget locator, String coordString) {
		// fixed for webdriver
		//super.doubleClickAt(locator.toLocator(), coordString);
		Scripts.triggerMouseEventAt(getWebDriver(), locator, "dblclick", coordString);
	}

	public void dragdrop(ClientWidget locator, String movementsString) {
		super.dragdrop(locator.toLocator(), movementsString);
	}

	public void dragAndDrop(ClientWidget locator, String movementsString) {
		super.dragAndDrop(locator.toLocator(), movementsString);
	}

	public void dragAndDropToObject(ClientWidget locatorOfObjectToBeDragged,
									ClientWidget locatorOfDragDestinationObject) {
		super.dragAndDropToObject(locatorOfObjectToBeDragged.toLocator(), locatorOfDragDestinationObject.toLocator());
	}

	public void dragdropTo(ClientWidget locatorOfObjectToBeDragged, String from, String to) {

		if (isIE()) {
			String[] froms = from.split(",");
			String[] tos = to.split(",");
			int x0 = (int) Double.parseDouble(froms[0]);
			int y0 = (int) Double.parseDouble(froms[1]);
			int x1 = (int) Double.parseDouble(tos[0]);
			int y1 = (int) Double.parseDouble(tos[1]);
			WebElement element = findElement(locatorOfObjectToBeDragged);
			// 0,0 means no offset, move to element center
			if (froms[0].equals("0") && froms[1].equals("0"))
				getActions().moveToElement(element).clickAndHold().moveByOffset(x1 - x0, y1 - y0).release().perform();
			else
				getActions().moveToElement(element, x0, y0).clickAndHold().moveByOffset(x1 - x0, y1 - y0).release()
						.perform();
		} else {
			// fixed for Selenium 2.5.0 issue
			Scripts.triggerMouseEventAt(getWebDriver(), locatorOfObjectToBeDragged, "mousemove", from);
			Scripts.triggerMouseEventAt(getWebDriver(), locatorOfObjectToBeDragged, "mousedown", from);
			if (isChrome())
				sleep(Integer.parseInt(ConfigHelper.getInstance().getDelay()));
			Scripts.triggerMouseEventAt(getWebDriver(), locatorOfObjectToBeDragged, "mousemove", to);
			Scripts.triggerMouseEventAt(getWebDriver(), locatorOfObjectToBeDragged, "mouseup", to);
		}

	}

	public void dragdropToObject(ClientWidget locatorOfObjectToBeDragged, ClientWidget locatorOfDragDestinationObject,
			String from, String to) {
		// fixed for Selenium 2.5.0
		mouseMoveAt(locatorOfObjectToBeDragged, from);
		waitResponse();
		mouseDownAt(locatorOfObjectToBeDragged, from);
		waitResponse();
		mouseMoveAt(locatorOfDragDestinationObject, to);
		waitResponse();
		mouseUpAt(locatorOfDragDestinationObject, to);
		waitResponse();
	}

	public void fireEvent(ClientWidget locator, String eventName) {
		super.fireEvent(locator.toLocator(), eventName);
	}

	/**
	 * @param locator
	 */
	public void focus(ClientWidget locator) {
		// fixed for IE9 on Webdriver
		if (isIE())
			zk(locator).focus();
		else
			super.focus(locator.toLocator());
	}

	/**
	 * @param locator
	 */
	public void blur(ClientWidget locator) {
		// fixed for IE9 on Webdriver
		// very tricky way to fire the blur event. In this case we cannot send Keys.Tab,
		// because it may affect the scrollbar to move.
		if (isIE())
			Scripts.triggerMouseEventAt(getWebDriver(), locator, "blur", "2,2");
		else
			super.fireEvent(locator.toLocator(), "blur");
	}

	public String getAttribute(ClientWidget attributeLocator) {
		return super.getAttribute(attributeLocator.toLocator());
	}

	public Number getCursorPosition(ClientWidget locator) {
		return super.getCursorPosition(locator.toLocator());
	}

	public Number getElementHeight(ClientWidget locator) {
		return super.getElementHeight(locator.toLocator());
	}

	public Number getElementIndex(ClientWidget locator) {
		return super.getElementIndex(locator.toLocator());
	}

	public Number getElementPositionLeft(ClientWidget locator) {
		return super.getElementPositionLeft(locator.toLocator());
	}

	public Number getElementPositionTop(ClientWidget locator) {
		return super.getElementPositionTop(locator.toLocator());
	}

	public Number getElementWidth(ClientWidget locator) {
		return super.getElementWidth(locator.toLocator());
	}

	public String[] getSelectOptions(ClientWidget selectLocator) {
		return super.getSelectOptions(selectLocator.toLocator());
	}

	public String getSelectedId(ClientWidget selectLocator) {
		return super.getSelectedId(selectLocator.toLocator());
	}

	public String[] getSelectedIds(ClientWidget selectLocator) {
		return super.getSelectedIds(selectLocator.toLocator());
	}

	public String getSelectedIndex(ClientWidget selectLocator) {
		return getCurrent().getSelectedIndex(selectLocator.toLocator());
	}

	public String[] getSelectedIndexes(ClientWidget selectLocator) {
		return super.getSelectedIndexes(selectLocator.toLocator());
	}

	public String getSelectedLabel(ClientWidget selectLocator) {
		return super.getSelectedLabel(selectLocator.toLocator());
	}

	public String[] getSelectedLabels(ClientWidget selectLocator) {
		return super.getSelectedLabels(selectLocator.toLocator());
	}

	public String getSelectedValue(ClientWidget selectLocator) {
		return super.getSelectedValue(selectLocator.toLocator());
	}

	public String[] getSelectedValues(ClientWidget selectLocator) {
		return super.getSelectedValues(selectLocator.toLocator());
	}

	public String getTable(ClientWidget tableCellAddress) {
		return super.getTable(tableCellAddress.toLocator());
	}

	public void highlight(ClientWidget locator) {
		super.highlight(locator.toLocator());
	}

	public boolean isChecked(ClientWidget locator) {
		return super.isChecked(locator.toLocator());
	}

	public boolean isEditable(ClientWidget locator) {
		return super.isEditable(locator.toLocator());
	}

	public boolean isElementPresent(ClientWidget locator) {
		return super.isElementPresent(locator.toLocator());
	}

	public boolean isOrdered(ClientWidget locator1, ClientWidget locator2) {
		return super.isOrdered(locator1.toLocator(), locator2.toLocator());
	}

	public boolean isSomethingSelected(ClientWidget selectLocator) {
		return super.isSomethingSelected(selectLocator.toLocator());
	}

	@Override
	public boolean isTextPresent(String pattern) {
		return super.isTextPresent(pattern);
	}

	public void keyDown(ClientWidget locator, String keySequence) {
		super.keyDown(locator.toLocator(), keySequence);
	}

	/**
	 * 2010/10/29 TonyQ:note: when typing number in chrome , it failed 
	 * @param locator
	 * @param keySequence
	 */
	public void keyPress(ClientWidget locator, String keySequence) {
		super.keyPress(locator.toLocator(), keySequence);
	}

	/**
	 * Use this method to simulate typing into an element, which may set its value.
	 * Notice: The element must get a focus before sendKeys. Most likely a click action is needed before calling this method.
	 *
	 * @param locator The locating mechanism to use
	 * @param keysToSend
	 * @since 2.0.0
	 */
	public void sendKeys(ClientWidget locator, CharSequence... keysToSend) {
		// fixed firefox Keys.ENTER is 14, not 13
		if (isFirefox()) {
			for (int i = 0; i < keysToSend.length; i++)
				if (keysToSend[i] == Keys.ENTER)
					keysToSend[i] = Keys.RETURN;
			getActions().sendKeys(keysToSend).perform();
			return;
		}
		// fixed Safari send 0..9
		if (isSafari()) {
			for (int i = 0; i < keysToSend.length; i++) {
				StringBuilder chseq = new StringBuilder(keysToSend[i]);
				boolean modified = false;
				for (int j = 0; j < chseq.length(); j++) {
					char c = chseq.charAt(j);
					if (c >= '0' && c <= '9') {
						chseq.deleteCharAt(j).insert(j, Keys.valueOf("NUMPAD" + c));
						modified = true;
					}
				}
				if (modified)
					keysToSend[i] = chseq;
			}
			typeKeys(locator, keysToSend.toString());
			return;
		}
		try {
			getWebDriver().findElement(locator.toBy()).sendKeys(keysToSend);
		} catch (InvalidSelectorException e) {
			getActions().sendKeys(keysToSend).perform();
		}
	}

	/**
	 * <pre>
	 * 2010/10/27 TonyQ:
	 * because there exist a lot of problem to press enter for ENTER key ,
	 * so we build the method for it.
	 *
	 * <b>NOTICE:</b>Because we use the keyPressNative , so you need to
	 * 	let computer focus on browser when you run the test case  which use this method,
	 *  or the native key press will NOT work anyway.
	 *
	 * If you want modify this ,please make sure that browser compatibility is ok .
	 * This is a hard method anyway.
	 *
	 * It is a issue for selenium.
	 * 
	 * Note: it will trigger onChanging that we unexpect.
	 * </pre>
	 * Browsers: firefox,safari402,chrome,ie8,ie7,ie6 .
	 */
	public void keyPressEnter(ClientWidget locator) {
		focus(locator);
		keyDown(locator, "13");
	}

	public void keyUp(ClientWidget locator, String keySequence) {
		super.keyUp(locator.toLocator(), keySequence);
	}

	public void mouseDown(ClientWidget locator) {
		super.mouseDown(locator.toLocator());
	}

	public void mouseDownAt(ClientWidget locator, String coordString) {
		if (isIE()) {
			//bug for Form.ztl 
			//Scripts.triggerMouseEventAt(getWebDriver(), locator, "mousedown", coordString);
			String[] froms = coordString.split(",");
			int x0 = Integer.parseInt(froms[0]);
			int y0 = Integer.parseInt(froms[1]);
			WebElement element = findElement(locator);
			getActions().moveToElement(element, x0, y0).clickAndHold(element).perform();
		} else {
			// fixed for Selenium 2.5.0
			// super.mouseDownAt(locator.toLocator(), coordString);
			Scripts.triggerMouseEventAt(getWebDriver(), locator, "mousedown", coordString);
		}
	}

	public void mouseDownRight(ClientWidget locator) {
		super.mouseDownRight(locator.toLocator());
	}

	public void mouseDownRightAt(ClientWidget locator, String coordString) {
		super.mouseDownRightAt(locator.toLocator(), coordString);
	}

	public void mouseMove(ClientWidget locator) {
		super.mouseMove(locator.toLocator());
	}

	public void mouseMoveAt(ClientWidget locator, String coordString) {
		if (isIE()) {
			String[] froms = coordString.split(",");
			int x0 = Integer.parseInt(froms[0]);
			int y0 = Integer.parseInt(froms[1]);
			WebElement element = findElement(locator);
			getActions().moveToElement(element, x0, y0).perform();
		} else {
			// fixed for Selenium 2.5.0
			Scripts.triggerMouseEventAt(getWebDriver(), locator, "mouseover", coordString);
		}
	}

	public void mouseOut(ClientWidget locator) {
		Scripts.triggerMouseEventAt(getWebDriver(), locator, "mouseout", "2,2");
	}

	public void mouseOver(By locator) {
		Scripts.triggerMouseEventAt(getWebDriver(), locator, "mouseover", "2,2");
	}

	public void mouseUp(ClientWidget locator) {
		Scripts.triggerMouseEventAt(getWebDriver(), locator, "mouseup", "1,1");
	}

	public void mouseUpAt(ClientWidget locator, String coordString) {
		// fixed for Selenium 2.5.0
		Scripts.triggerMouseEventAt(getWebDriver(), locator, "mouseup", coordString);
	}

	public void mouseUpRight(ClientWidget locator) {
		super.mouseUpRight(locator.toLocator());
	}

	public void mouseUpRightAt(ClientWidget locator, String coordString) {
		super.mouseUpRightAt(locator.toLocator(), coordString);
	}

	public void removeAllSelections(ClientWidget locator) {
		super.removeAllSelections(locator.toLocator());
	}

	public void removeSelection(ClientWidget locator, String optionLocator) {
		super.removeSelection(locator.toLocator(), optionLocator);
	}

	public void rollup(ClientWidget rollupName, String kwargs) {
		super.rollup(rollupName.toLocator(), kwargs);
	}

	public void selectFrame(ClientWidget locator) {
		if (locator instanceof Widget)
			getWebDriver().switchTo().frame(((Widget) locator).uuid().toString());
		else
			getWebDriver().switchTo().frame(findElement(locator));
	}

	public void setContext(ClientWidget context) {
		super.setContext(context.toLocator());
	}

	public void setCursorPosition(ClientWidget locator, String position) {
		super.setCursorPosition(locator.toLocator(), position);
	}

	public void submit(ClientWidget formLocator) {
		super.submit(formLocator.toLocator());
	}

	/**
	 * 
	 * @param locator
	 * @param percent
	 * 
	 */
	public void verScroll(ClientWidget locator, double percent) {
		int totalHight;
		Widget wgt = jq(locator).toWidget();
		Element bpad = wgt.$n("bpad"),
				tpad = wgt.$n("tpad");
		JQuery body = jq(wgt.$n("body")),
			   cave = jq(wgt.$n("cave"));
		totalHight = jq(cave).height() - jq(body).height();
		if (bpad.exists() && tpad.exists()) {
			// ROD Scroll
			totalHight += Integer.parseInt(bpad.attr("offsetHeight")) + Integer.parseInt(tpad.attr("offsetHeight"));
		}
		
		int dist = (int) Math.round(totalHight * percent / 100);

		verScrollAbs(locator, dist);
	}
	/**
	 *
	 * @param locator
	 * @param dist
	 *
	 */
	public void verScrollAbs(ClientWidget locator, int dist) {
		int totalHight;
		Widget wgt = jq(locator).toWidget();
		JQuery body = jq(wgt.$n("body")),
				cave = jq(wgt.$n("cave"));
		if(!hasNativeScroll(locator))
			locator.eval("_scrollbar.scrollTo(0, " + dist +")");
		else
			jq(body.exists() ? body : (cave.exists() ? cave : wgt))
			.toElement().set("scrollTop", Math.abs(dist));
		waitResponse();
	}
	/**
	 * 
	 * @param locator
	 * @param percent
	 * 
	 */
	public void horScroll(ClientWidget locator, double percent) {
		Widget wgt = jq(locator).toWidget();
		JQuery body = jq(wgt.$n("body"));
		JQuery cave = jq(wgt.$n("cave"));
		int totalWidth = cave.width() - body.width();		
		int dist = (int) Math.round(totalWidth * percent / 100);

		horScrollAbs(locator,  dist);
	}
	/**
	 *
	 * @param locator
	 * @param dist
	 *
	 */
	public void horScrollAbs(ClientWidget locator, int dist) {
		Widget wgt = jq(locator).toWidget();
		JQuery body = jq(wgt.$n("body"));
		JQuery cave = jq(wgt.$n("cave"));
		if(!hasNativeScroll(locator))
			locator.eval("_scrollbar.scrollTo(" + dist +", 0)");
		else 
			jq(body.exists() ? body : (cave.exists() ? cave : wgt))
			.toElement().set("scrollLeft", Math.abs(dist));
		waitResponse();
	}
	
	/**
	 * In a case that widget only has 'cave' but 'body',
	 * we will calculate sum of all children's width of cave,
	 * and move widget's scrollbar
	 * @param locator
	 * @param percent
	 * 
	 */
	public void horScrollNoBody(ClientWidget locator, double percent) {
		Widget wgt = jq(locator).toWidget();
		JQuery cave = jq(wgt.$n("cave"));
		int childrenWidth = 0;
		for (JQuery child : cave.children()) {
			childrenWidth += child.width();
		}
		int totalWidth = childrenWidth - jq(locator).width();		
		int dist = (int) Math.round(totalWidth * percent);

		horScrollNoBodyAbs(locator, dist);
	}
	/**
	 * In a case that widget only has 'cave' but 'body',
	 * we will calculate sum of all children's width of cave,
	 * and move widget's scrollbar
	 * @param locator
	 * @param dist
	 *
	 */
	public void horScrollNoBodyAbs(ClientWidget locator, int dist) {
		Widget wgt = jq(locator).toWidget();
		if(!hasNativeScroll(locator))
			locator.eval("_scrollbar.scrollTo(" + dist +", 0)");
		else
			jq(wgt).toElement().set("scrollLeft", Math.abs(dist));
		waitResponse();
	}
	
	/**
	 * In a case that widget only has 'cave' but 'body',
	 * we will calculate sum of all children's height of cave,
	 * and move widget's scrollbar
	 * @param locator
	 * @param percent
	 * 
	 */
	public void verScrollNoBody(ClientWidget locator, double percent) {
		Widget wgt = jq(locator).toWidget();
		JQuery cave = jq(wgt.$n("cave"));
		int childrenHeight = 0;
		for (JQuery child : cave.children()) {
			childrenHeight += child.height();
		}
		int totalHeight = childrenHeight - jq(locator).height();		
		int dist = (int) Math.round(totalHeight * percent);
		
		verScrollNoBodyAbs(locator, dist);
	}

	/**
	 * In a case that widget only has 'cave' but 'body',
	 * we will calculate sum of all children's height of cave,
	 * and move widget's scrollbar
	 * @param locator
	 * @param dist
	 *
	 */
	public void verScrollNoBodyAbs(ClientWidget locator, int dist) {
		Widget wgt = jq(locator).toWidget();
		if(!hasNativeScroll(locator))
			locator.eval("_scrollbar.scrollTo(" + dist +", 0)");
		else
			jq(wgt).toElement().set("scrollTop", Math.abs(dist));
		waitResponse();
	}
	/**
	 * detect if it has a horizontal scroll bar
	 * @param locator
	 * @return
	 */
	public boolean hasHScrollbar(ClientWidget locator) {
		if (hasNativeScroll(jq(locator).toWidget())) {
			return Boolean.parseBoolean(zk(locator).eval("hasHScroll()"));
		} else
			return jq(locator).find(".z-scrollbar-horizontal").exists();
	}
	
	/**
	 * detect if it has a vertical scroll bar
	 * @param locator
	 * @return
	 */
	public boolean hasVScrollbar(ClientWidget locator) {
		if(hasNativeScroll(jq(locator).toWidget())) {
			return Boolean.parseBoolean(zk(locator).eval("hasVScroll()"));
		} else
			return jq(locator).find(".z-scrollbar-vertical").exists();
	}
	
	public void doScroll(ClientWidget locatorOfObjectToBeDragged,
			ClientWidget locatorOfDragDestinationObject, String from, String to) {
		mouseMoveAt(locatorOfObjectToBeDragged, from);
		mouseDownAt(locatorOfObjectToBeDragged, from);
		waitResponse();
		mouseMoveAt(locatorOfDragDestinationObject, to);
		waitResponse();
		mouseUpAt(locatorOfDragDestinationObject, to);
	}
	
	public int getScrollTop(Widget widget) {
		if (!hasNativeScroll(widget)) {
			String str =  jq(widget).find(".z-scrollbar").toElement().attr("style.top").trim();
			return Integer.parseInt(str.substring(0, str.lastIndexOf("px")));
		} else {
			return jq(widget.$n("body")).scrollTop();
		}
	}
	
	public int getScrollLeft(Widget widget) {
		if (!hasNativeScroll(widget)) {
			String str =  jq(widget).find(".z-scrollbar").toElement().attr("style.left").trim();
			return Integer.parseInt(str.substring(0, str.lastIndexOf("px")));
		} else {
			return jq(widget.$n("body")).scrollLeft();
		}
	}
		
	/**
	 * Types the value to the locator.
	 * <p> The method will call focus() before typing and blur() after typed.
	 */
	public void type(ClientWidget locator, String value) {
		if (isOpera()) {
			findElement(locator).clear();
			waitResponse();
			typeKeys(locator, value);
			waitResponse();
		} else {
			focus(locator);
			super.type(locator.toLocator(), value);
			blur(locator);
		}
	}

	/**
	 * Types the value to the locator.
	 * <p> The method will call focus() before typing and blur() after typed.
	 */
	public void typeKeys(ClientWidget locator, String value) {
		focus(locator);
		super.typeKeys(locator.toLocator(), value);
		blur(locator);
	}

	public void uncheck(ClientWidget locator) {
		super.uncheck(locator.toLocator());
	}

	/**
	 * Performs a single tap on the element found by locator, analogous to click using a mouse 
	 * @param locator an element locator
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void singleTap(ClientWidget locator) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		WebElement toTap = findElement(locator);
		new TouchActions(driver).singleTap(toTap).perform();
	}

	/**
	 * Performs a single tap on the element found by locator tanalogous to click using a mouse
	 * The tap position is at an offset position.
	 * @param locator an element locator
	 * @param xOffset px toward the right
	 * @param yOffset px toward the bottom
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void singleTapAt(ClientWidget locator, int xOffset, int yOffset) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		Point tap = findElement(locator.toBy()).getLocation().moveBy(xOffset, yOffset);
		new TouchActions(driver).down(tap.x, tap.y).up(tap.x, tap.y).perform();
	}

	/**
	 * Performs a double tap on the element found by locator, analogous to double click using a mouse
	 * @param locator an element locator
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void doubleTap(ClientWidget locator) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		WebElement toTap = findElement(locator);
		new TouchActions(driver).doubleTap(toTap).perform();
	}

	/**
	 * Performs a long press gesture on the element found by locator
	 * @param locator an element locator
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void longPress(ClientWidget locator) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		WebElement toPress = findElement(locator);
		new TouchActions(driver).longPress(toPress).perform();
	}

	private static final int SWIPE_STEPS = 5;

	/**
	 * Swipe up at the bottom edge of the element found by locator
	 * @param locator  an element locator
	 * @param distance amount to swipe (in device pixel)
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void swipeUp(ClientWidget locator, int distance) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		WebElement toSwipe = findElement(locator.toBy());
		Point topLeft = toSwipe.getLocation();
		Dimension size = toSwipe.getSize();

		Point origin = topLeft.moveBy(size.getWidth() / 2, size.getHeight() - 1);
		Point dest = origin.moveBy(0, -distance);

		double yStep = (dest.getY() - origin.getY()) / SWIPE_STEPS;
		int x = origin.getX();
		int y = origin.getY();

		TouchActions actions = new TouchActions(driver);
		actions.down(x, y);
		for (int i = 0; i < SWIPE_STEPS; i++) {
			y += yStep;
			actions.move(x, y);
		}
		actions.up(dest.getX(), dest.getY());
		actions.perform();
	}

	/**
	 * Swipe down at the top edge of the element found by locator
	 * @param locator  an element locator
	 * @param distance amount to swipe (in device pixel)
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void swipeDown(ClientWidget locator, int distance) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		WebElement toSwipe = findElement(locator.toBy());
		Point topLeft = toSwipe.getLocation();
		Dimension size = toSwipe.getSize();

		Point origin = topLeft.moveBy(size.getWidth() / 2, 1);
		Point dest = origin.moveBy(0, distance);

		double yStep = (dest.getY() - origin.getY()) / SWIPE_STEPS;
		int x = origin.getX();
		int y = origin.getY();

		TouchActions actions = new TouchActions(driver);
		actions.down(x, y);
		for (int i = 0; i < SWIPE_STEPS; i++) {
			y += yStep;
			actions.move(x, y);
		}
		actions.up(dest.getX(), dest.getY());
		actions.perform();
	}

	/**
	 * Swipe left at the right edge of the element found by locator
	 * @param locator  an element locator
	 * @param distance amount to swipe (in device pixel)
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void swipeLeft(ClientWidget locator, int distance) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		WebElement toSwipe = findElement(locator.toBy());
		Point topLeft = toSwipe.getLocation();
		Dimension size = toSwipe.getSize();

		Point origin = topLeft.moveBy(size.getWidth() - 1, size.getHeight() / 2);
		Point dest = origin.moveBy(-distance, 0);

		double xStep = (dest.getX() - origin.getX()) / SWIPE_STEPS;
		int x = origin.getX();
		int y = origin.getY();

		TouchActions actions = new TouchActions(driver);
		actions.down(x, y);
		for (int i = 0; i < SWIPE_STEPS; i++) {
			x += xStep;
			actions.move(x, y);
		}
		actions.up(dest.getX(), dest.getY());
		actions.perform();
	}

	/**
	 * Swipe right at the left edge of the element found by locator
	 * @param locator  an element locator
	 * @param distance amount to swipe (in device pixel)
	 * @since 2.0.1
	 * Category - HasTouchScreen
	 */
	public void swipeRight(ClientWidget locator, int distance) {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof HasTouchScreen))
			throw new UnsupportedOperationException("device not touchable");

		WebElement toSwipe = findElement(locator.toBy());
		Point topLeft = toSwipe.getLocation();
		Dimension size = toSwipe.getSize();

		Point origin = topLeft.moveBy(1, size.getHeight() / 2);
		Point dest = origin.moveBy(distance, 0);

		double xStep = (dest.getX() - origin.getX()) / SWIPE_STEPS;
		int x = origin.getX();
		int y = origin.getY();

		TouchActions actions = new TouchActions(driver);
		actions.down(x, y);
		for (int i = 0; i < SWIPE_STEPS; i++) {
			x += xStep;
			actions.move(x, y);
		}
		actions.up(dest.getX(), dest.getY());
		actions.perform();
	}

	/**
	 * Returns the current device orientation
	 * @return the current device orientation
	 * @since 2.0.1
	 * Category - Rotatable
	 */
	public String getOrientation() {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof Rotatable))
			throw new UnsupportedOperationException("device not rotatable");

		Rotatable device = (Rotatable) driver;
		ScreenOrientation orient = device.getOrientation();
		if (ScreenOrientation.PORTRAIT.equals(orient)) {
			return "portrait";
		} else {
			return "landscape";
		}
	}

	/**
	 * Rotate the device
	 * @since 2.0.1
	 * Category - Rotatable
	 */
	public void rotate() {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof Rotatable))
			throw new UnsupportedOperationException("device not rotatable");

		Rotatable device = (Rotatable) driver;
		ScreenOrientation orient = device.getOrientation();
		if (ScreenOrientation.LANDSCAPE.equals(orient)) {
			device.rotate(ScreenOrientation.PORTRAIT);
		} else {
			device.rotate(ScreenOrientation.LANDSCAPE);
		}
	}

	/**
	 * Ensures the device is in landscape orientation
	 * @since 2.0.1
	 * Category - Rotatable
	 */
	public void ensureLandscape() {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof Rotatable))
			throw new UnsupportedOperationException("device not rotatable");

		Rotatable device = (Rotatable) driver;
		ScreenOrientation orient = device.getOrientation();
		if (ScreenOrientation.PORTRAIT.equals(orient)) {
			device.rotate(ScreenOrientation.LANDSCAPE);
		}
	}

	/**
	 * Ensures the device is in portrait orientation
	 * @since 2.0.1
	 * Category - Rotatable
	 */
	public void ensurePortrait() {
		WebDriver driver = getWebDriver();
		if (!(driver instanceof Rotatable))
			throw new UnsupportedOperationException("device not rotatable");

		Rotatable device = (Rotatable) driver;
		ScreenOrientation orient = device.getOrientation();
		if (ScreenOrientation.LANDSCAPE.equals(orient)) {
			device.rotate(ScreenOrientation.PORTRAIT);
		}
	}

	/**
	 * Press shift and click items
	 * @param locators
	 */
	public void shiftClickItems(List<ClientWidget> locators) {
		for (ClientWidget locator : locators) {
			shiftKeyDown();
			click(locator);
			waitResponse();
		}
		shiftKeyUp();
		waitResponse();
	}

	/**
	 * Returns boolean to indicate if there is any js error or erro window
	 * @return
	 */
	protected boolean hasError() {
		return Boolean.valueOf(Scripts.getEval("!!jq('.z-messagebox-error')[0] || !!jq('.z-errorbox')[0] || jq('.z-error')[0]"));
	}

	/**
	 * Close the errorbox for webdriver
	 * @since 2.0.0
	 */
	public void closeErrorBox() {
		// fixed bug for B50-2916148.ztl
		JQuery jq = jq(".z-errbox-close");
		int x = jq.width() - 3;
		x += parseInt(jq.css("padding-right"));

		if (isSafari() || isFirefox())
			Scripts.triggerMouseEventAt(getWebDriver(), jq, "click", x + ",3");
		else {
			WebElement element = null;
			if (isAndroid()) {
				element = findElement(jq.toBy());
				Point p = element.getLocation();
				x += p.x;
				new TouchActions(getWebDriver()).down(x, p.y + 3).up(x, p.y + 3).perform();
			} else {
				element = findElement(jq);
				getActions().moveToElement(element, x, 3).click().perform();
			}

			// double check again (clicking without padding-right)
			// sometimes on 64 bit OS will need to close again for IE9.
			if (jq.exists() && !isAndroid()) {
				x -= parseInt(jq.css("padding-right"));
				getActions().moveToElement(element, x, 3).click().perform();
			}
		}
	}

	public boolean isVisible(ClientWidget locator) {
		return jq(locator).isVisible();
	}

	public String getText(ClientWidget locator) {
		return super.getText(locator.toLocator());
	}

	public String getValue(ClientWidget locator) {
		return super.getValue(locator.toLocator());
	}

	public void select(ClientWidget selectLocator, String optionLocator) {
		if (isIE()) {
			click(jq(selectLocator).find("option:contains(\"" + optionLocator + "\")"));
		} else if (!isSafari()) {
			findElement(jq(selectLocator).find("option:contains(\"" + optionLocator + "\")")).click();
			// force to fire onChange event for IE
			if (isIE())
				blur(selectLocator);
		} else {
			//use javascript to simulate
			String id = selectLocator.toElement().attr("id").toString();
			executeScript("jq('#" + id + "').val('" + optionLocator + "');");
			waitResponse();
			executeScript("jq('#" + id + "').change()");
			waitResponse();
		}
	}
	public void select(ClientWidget selectLocator, int index) {
		if (!isSafari()) {
			// In IE, it will fail on B30-1819318.ztl, we may wait for the latest version
			// of selenium 2.2+ to fix the following API.
			// new Select(findElement(selectLocator)).selectByVisibleText(optionLocator);
			findElement(jq(selectLocator).find("option").get(index)).click();

			// force to fire onChange event for IE
			if (isIE())
				blur(selectLocator);
		} else {
			//use javascript to simulate
			String id = selectLocator.toElement().attr("id").toString();
			String selectionTargetScript = "jq('#" + id + "').children()[" + index + "].text";
			executeScript("jq('#" + id + "').val(" + selectionTargetScript + ");");
			waitResponse();
			executeScript("jq('#" + id + "').change()");
			waitResponse();
		}
	}
	/**
	 * @param locator
	 * @param num
	 */
	public void frozenScroll(ClientWidget locator, int num) {
		Widget wgt = jq(locator).toWidget();
		wgt.eval("frozen._doScrollNow(" + num + ")");
		waitResponse();
	}
	/**
	 * @param locator
	 * @param dist
	 */
	public void nativeFrozenScroll(ClientWidget locator, double dist) {
		String lo = locator.toLocator();

		Widget wgt = jq(locator).find(".z-frozen").toWidget();
		jq(wgt.$n("scrollX")).toElement().set("scrollLeft", "" + dist);
		waitResponse();
	}

	public boolean hasNativeScroll(ClientWidget locator) {
		return Boolean.valueOf(Scripts.getEval("!" + locator.toLocator() + "._scrollbar"));
	}

	/**
	 * a shortcut for getting alert message.
	 * (Take care about the dom class and model will be different
	 * 	when event-thread is enable/disable , so we use title .)
	 * @return
	 */
	public String getAlertMessage() {
		return jq("@window[title=\"ZK Test\"] @label").text();
	}

	/**
	 * a shortcut to get the value in zk.log textarea
	 * @return
	 */
	public String getZKLog() {
		return jq("#zk_log").val().trim();
	}

	/**
	 * a shortcut to close zk log
	 */
	public void closeZKLog() {
		Scripts.getEval("!!jq('#zk_logbox').remove()", null);
		waitResponse();
	}

	public void clickAlert() {
		click(jq("@window[title=\"ZK Test\"] @button"));
	}

	/**
	 * Switch to the next tab by sending "CTRL + TAB" to the browser, if current
	 * tab is the last tab, will switch to the first tab.
	 * <p>
	 * Will also switch WebDriver's focus to the newly switched tab, so other
	 * operations can proceed as normal.
	 */
	public void switchTab() {
		WebDriver driver = getWebDriver();
		List<String> tabs = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tabs.get(0));
	}

	/* migrate from ZK.java */
	/**
	 * Returns the boolean value from the evaluated name.
	 * <p>For example,
	 * <code>ZK.is("ie");</code>
	 * The invoking JavaScript code will be "zk.ie", and return the boolean value.
	 */
	public boolean is(String name) {
		return Boolean.valueOf(Scripts.getEval("!!(zk." + name + ")"));
	}

	protected int parseInt(String number) {
		if (number != null) {
			number = number.replaceAll("[^-0-9\\.]", "");
			int decimal = number.indexOf('.');
			if (decimal > 0)
				number = number.substring(0, decimal);

			if(!"".equals(number.trim())){
				return Integer.parseInt(number);
			}else{
				return 0;
			}
		}
		return 0;
	}

	protected float parseFloat(String number) {
		if (number != null) {
			number = number.replaceAll("[^-0-9\\.]", "");
			if(!"".equals(number.trim())){
				return Float.parseFloat(number);
			}else{
				return 0.0F;
			}
		}
		return 0.0F;
	}

	protected double parseDouble(String number) {
		if (number != null) {
			number = number.replaceAll("[^-0-9\\.]", "");
			if(!"".equals(number.trim())){
				return Double.parseDouble(number);
			}else{
				return 0.0;
			}
		}
		return 0.0;
	}

	protected boolean parseBoolean(String bool) {
		return Boolean.valueOf(bool);
	}

	/**
	 * Returns window width
	 * @return
	 */
	public int getWindowWidth() {
		return getWebDriver().manage().window().getSize().width;
	}

	/**
	 * Returns window height
	 * @return
	 */
	public int getWindowHeight() {
		return getWebDriver().manage().window().getSize().height;
	}

	/**
	 * Set window size
	 * @param width
	 * @param height
	 */
	public void setWindowSize(int width, int height) {
		getWebDriver().manage().window().setSize(new Dimension(width, height));
	}

	/**
	 * navigate page back or forward
	 * @param forword if true, navigate forward
	 */
	public void navigatePage(boolean forword) {
		if (forword)
			getWebDriver().navigate().forward();
		else
			getWebDriver().navigate().back();
	}

	public int getBrowserTabCount() {
		return getWebDriver().getWindowHandles().size();
	}
}
