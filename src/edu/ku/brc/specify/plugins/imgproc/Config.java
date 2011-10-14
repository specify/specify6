/*
 * Copyright 2011 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.ku.brc.specify.plugins.imgproc;

import com.google.zxing.DecodeHintType;

import java.util.Hashtable;

public final class Config {

  private Hashtable<DecodeHintType, Object> hints;
  private boolean tryHarder;
  private boolean pureBarcode;
  private boolean productsOnly;
  private boolean dumpResults;
  private boolean dumpBlackPoint;
  private boolean multi;
  private boolean brief;
  private boolean recursive;
  private int[] crop;
  private int threads = 1;

  public Hashtable<DecodeHintType, Object> getHints() {
    return hints;
  }

  public void setHints(Hashtable<DecodeHintType, Object> hints) {
    this.hints = hints;
  }

  public boolean isTryHarder() {
    return tryHarder;
  }

  public void setTryHarder(boolean tryHarder) {
    this.tryHarder = tryHarder;
  }

  public boolean isPureBarcode() {
    return pureBarcode;
  }

  public void setPureBarcode(boolean pureBarcode) {
    this.pureBarcode = pureBarcode;
  }

  public boolean isProductsOnly() {
    return productsOnly;
  }

  public void setProductsOnly(boolean productsOnly) {
    this.productsOnly = productsOnly;
  }

  public boolean isDumpResults() {
    return dumpResults;
  }

  public void setDumpResults(boolean dumpResults) {
    this.dumpResults = dumpResults;
  }

  public boolean isDumpBlackPoint() {
    return dumpBlackPoint;
  }

  public void setDumpBlackPoint(boolean dumpBlackPoint) {
    this.dumpBlackPoint = dumpBlackPoint;
  }

  public boolean isMulti() {
    return multi;
  }

  public void setMulti(boolean multi) {
    this.multi = multi;
  }

  public boolean isBrief() {
    return brief;
  }

  public void setBrief(boolean brief) {
    this.brief = brief;
  }

  public boolean isRecursive() {
    return recursive;
  }

  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  public int[] getCrop() {
    return crop;
  }

  public void setCrop(int[] crop) {
    this.crop = crop;
  }

  public int getThreads() {
    return threads;
  }

  public void setThreads(int threads) {
    this.threads = threads;
  }
}
