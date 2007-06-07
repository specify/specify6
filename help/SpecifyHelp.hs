<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<?TestTarget this is data for the test target ?>

<helpset version="1.0">

  <!-- title -->
  <title>Specify Help</title>

  <!-- maps -->
  <maps>
     <homeID>main</homeID>
     <mapref location="SpecifyHelp.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Specify</label>
    <type>javax.help.TOCView</type>
    <data>SpecifyHelpTOC.xml</data>
  </view>


  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>SpecifyHelpIndex.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>
  
  <!--  <view>
  <view>
    <name>Glossary</name>
    <label>Glossary</label>
    <type>javax.help.GlossaryView</type>
    <data>SpecifyHelpGlossary.xml</data>
  </view>
-->
  <view>
    <name>Favorites</name>
    <label>Favorites</label>
    <type>javax.help.FavoritesView</type>
    <data></data>
  </view>

</helpset>
