/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.uk.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractParser<ElementType extends Enum<ElementType>> {

  private static final byte[] UTF8_BOM = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};

  protected Map<String, ElementType> _typesByKey = new HashMap<String, ElementType>();

  protected Map<String, ExtensionParser> _extensionParsersByKey = new HashMap<String, ExtensionParser>();

  private ParserInstance _parserInstance = new ParserInstanceImpl();

  private String _currentPath = null;

  private int _currentLineNumber = 0;

  private String _currentLine;

  private int _currentLineCharactersConsumed;

  private int _elementTypeKeySize = 2;

  private String _ignoreElementTypeWithPrefix = null;

  /**
   * If an elementType value has the specified prefix, the element will be
   * ignored.
   * 
   * @param prefix
   */
  public void setIgnoreElementTypeWithPrefix(String prefix) {
    _ignoreElementTypeWithPrefix = prefix;
  }

  public void addExtension(String key, ExtensionParser parser) {
    _extensionParsersByKey.put(key, parser);
  }

  public void parse(File path, ContentHandler handler) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream(path), "UTF-8"));
    parse(path.getAbsolutePath(), reader, handler);
  }

  public void parse(String path, BufferedReader reader, ContentHandler handler)
      throws IOException {
    _currentPath = path;
    _currentLine = null;
    _currentLineNumber = 0;
    _currentLineCharactersConsumed = 0;

    handler.startDocument();

    while ((_currentLine = reader.readLine()) != null) {

      _currentLineCharactersConsumed = 0;
      _currentLineNumber++;
      if (_currentLineNumber == 1) {
        stripBom();
      }
      if (!parseLine(handler)) {
        break;
      }
    }
    closeOpenElementIfNeeded(null, handler);
    handler.endDocument();

    reader.close();
  }

  private void stripBom() {
    /**
     * Check for and strip the UTF-8 BOM
     */
    try {
      String prefix = peek(1);
      if (prefix.length() == 1
          && Arrays.equals(prefix.getBytes("UTF-8"), UTF8_BOM)) {
        pop(1);
      }
    } catch (UnsupportedEncodingException ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected boolean parseLine(ContentHandler handler) {
    String typeValue = pop(_elementTypeKeySize);
    ElementType type = _typesByKey.get(typeValue);
    if (type != null) {
      return handleRecordType(type, handler);
    }
    ExtensionParser extensionParser = _extensionParsersByKey.get(typeValue);
    if (extensionParser != null) {
      return extensionParser.handleExtensionRecordType(_parserInstance, handler);
    }
    if (_ignoreElementTypeWithPrefix != null
        && typeValue.startsWith(_ignoreElementTypeWithPrefix)) {
      return true;
    }

    throw new ParserException("unknown record type: " + typeValue + " at "
        + describeLineLocation());
  }

  protected void setElementTypeKeySize(int size) {
    _elementTypeKeySize = size;
  }

  /**
   * 
   * @param type
   * @param handler
   * @return false if parsing should not continue
   */
  protected abstract boolean handleRecordType(ElementType type,
      ContentHandler handler);

  protected boolean isFirstLine() {
    return _currentLineNumber == 1;
  }

  protected <T extends Element> T element(T element) {
    element.setFilePath(_currentPath);
    element.setLineNumber(_currentLineNumber);
    return element;
  }

  protected void fireElement(Element element, ContentHandler handler) {
    closeOpenElementIfNeeded(element, handler);
    handler.startElement(element);
    handler.endElement(element);
  }

  protected void closeOpenElementIfNeeded(Element element,
      ContentHandler handler) {

  }

  protected int integer(String value) {
    return Integer.parseInt(value);
  }

  protected Date parseDate(DateFormat format, String value) {
    try {
      return format.parse(value);
    } catch (ParseException ex) {
      throw new ParserException("error parsing date value=" + value, ex);
    }
  }

  protected String pop(int count) {
    if (_currentLine.length() < count) {
      throw new ParserException("expected line " + _currentLineNumber
          + " to have length of at least "
          + (_currentLineCharactersConsumed + count) + " but only found "
          + (_currentLineCharactersConsumed + _currentLine.length()));
    }
    String value = _currentLine.substring(0, count);
    _currentLine = _currentLine.substring(count);
    _currentLineCharactersConsumed += count;
    return value.trim();
  }

  private String peek(int count) {
    count = Math.min(count, _currentLine.length());
    return _currentLine.substring(0, count);
  }

  protected String describeLineLocation() {
    return "file=" + _currentPath + " line=" + _currentLineNumber;
  }

  private class ParserInstanceImpl implements ParserInstance {

    @Override
    public <T extends Element> T element(T element) {
      return AbstractParser.this.element(element);
    }

    @Override
    public String pop(int count) {
      return AbstractParser.this.pop(count);
    }

    @Override
    public void fireElement(Element element, ContentHandler handler) {
      AbstractParser.this.fireElement(element, handler);
    }
  }
}
