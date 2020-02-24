package edu.ku.brc.specify.datamodel;

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "taxonattribute")
public class TaxonAttribute extends DataModelObjBase implements Cloneable {
    protected Integer taxonAttributeId;

    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;
    protected String text6;
    protected String text7;
    protected String text8;
    protected String text9;
    protected String text10;
    protected String text11;
    protected String text12;
    protected String text13;
    protected String text14;
    protected String text15;
    protected String text16;
    protected String text17;
    protected String text18;
    protected String text19;
    protected String text20;
    protected String text21;
    protected String text22;
    protected String text23;
    protected String text24;
    protected String text25;
    protected String text26;
    protected String text27;
    protected String text28;
    protected String text29;
    protected String text30;
    protected String text31;
    protected String text32;
    protected String text33;
    protected String text34;
    protected String text35;
    protected String text36;
    protected String text37;
    protected String text38;
    protected String text39;
    protected String text40;
    protected String text41;
    protected String text42;
    protected String text43;
    protected String text44;
    protected String text45;
    protected String text46;
    protected String text47;
    protected String text48;
    protected String text49;
    protected String text50;
    protected String text51;
    protected String text52;
    protected String text53;
    protected String text54;
    protected String text55;
    protected String text56;
    protected String text57;
    protected String text58;

    protected Boolean yesNo1;
    protected Boolean yesNo2;
    protected Boolean yesNo3;
    protected Boolean yesNo4;
    protected Boolean yesNo5;
    protected Boolean yesNo6;
    protected Boolean yesNo7;
    protected Boolean yesNo8;
    protected Boolean yesNo9;
    protected Boolean yesNo10;
    protected Boolean yesNo11;
    protected Boolean yesNo12;
    protected Boolean yesNo13;
    protected Boolean yesNo14;
    protected Boolean yesNo15;
    protected Boolean yesNo16;
    protected Boolean yesNo17;
    protected Boolean yesNo18;
    protected Boolean yesNo19;
    protected Boolean yesNo20;
    protected Boolean yesNo21;
    protected Boolean yesNo22;
    protected Boolean yesNo23;
    protected Boolean yesNo24;
    protected Boolean yesNo25;
    protected Boolean yesNo26;
    protected Boolean yesNo27;
    protected Boolean yesNo28;
    protected Boolean yesNo29;
    protected Boolean yesNo30;
    protected Boolean yesNo31;
    protected Boolean yesNo32;
    protected Boolean yesNo33;
    protected Boolean yesNo34;
    protected Boolean yesNo35;
    protected Boolean yesNo36;
    protected Boolean yesNo37;
    protected Boolean yesNo38;
    protected Boolean yesNo39;
    protected Boolean yesNo40;
    protected Boolean yesNo41;
    protected Boolean yesNo42;
    protected Boolean yesNo43;
    protected Boolean yesNo44;
    protected Boolean yesNo45;
    protected Boolean yesNo46;
    protected Boolean yesNo47;
    protected Boolean yesNo48;
    protected Boolean yesNo49;
    protected Boolean yesNo50;
    protected Boolean yesNo51;
    protected Boolean yesNo52;
    protected Boolean yesNo53;
    protected Boolean yesNo54;
    protected Boolean yesNo55;
    protected Boolean yesNo56;
    protected Boolean yesNo57;
    protected Boolean yesNo58;
    protected Boolean yesNo59;
    protected Boolean yesNo60;
    protected Boolean yesNo61;
    protected Boolean yesNo62;
    protected Boolean yesNo63;
    protected Boolean yesNo64;
    protected Boolean yesNo65;
    protected Boolean yesNo66;
    protected Boolean yesNo67;
    protected Boolean yesNo68;
    protected Boolean yesNo69;
    protected Boolean yesNo70;
    protected Boolean yesNo71;
    protected Boolean yesNo72;
    protected Boolean yesNo73;
    protected Boolean yesNo74;
    protected Boolean yesNo75;
    protected Boolean yesNo76;
    protected Boolean yesNo77;
    protected Boolean yesNo78;
    protected Boolean yesNo79;
    protected Boolean yesNo80;
    protected Boolean yesNo81;
    protected Boolean yesNo82;

    protected Float number1;
    protected Float number2;
    protected Float number3;
    protected Float number4;
    protected Float number5;
    protected Float number6;
    protected Float number7;
    protected Float number8;
    protected Float number9;
    protected Float number10;
    protected Float number11;
    protected Float number12;
    protected Float number13;
    protected Float number14;
    protected Float number15;
    protected Float number16;
    protected Float number17;
    protected Float number18;
    protected Integer number19;
    protected Integer number20;

    protected Agent agent1;
    protected Calendar date1;
    protected Byte date1Precision;

    protected String remarks;

    protected Set<Taxon> taxons;

    // Constructors

    /**
     * default constructor
     */
    public TaxonAttribute() {
        // do nothing
    }

    /**
     * constructor with id
     */
    public TaxonAttribute(Integer taxonAttributeId) {
        this.taxonAttributeId = taxonAttributeId;
    }

    // Initializer
    @Override
    public void initialize() {
        super.init();

        taxonAttributeId = null;

        text1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        text6 = null;
        text7 = null;
        text8 = null;
        text9 = null;
        text10 = null;
        text11 = null;
        text12 = null;
        text13 = null;
        text14 = null;
        text15 = null;
        text16 = null;
        text17 = null;
        text18 = null;
        text19 = null;
        text20 = null;
        text21 = null;
        text22 = null;
        text23 = null;
        text24 = null;
        text25 = null;
        text26 = null;
        text27 = null;
        text28 = null;
        text29 = null;
        text30 = null;
        text31 = null;
        text32 = null;
        text33 = null;
        text34 = null;
        text35 = null;
        text36 = null;
        text37 = null;
        text38 = null;
        text39 = null;
        text40 = null;
        text41 = null;
        text42 = null;
        text43 = null;
        text44 = null;
        text45 = null;
        text46 = null;
        text47 = null;
        text48 = null;
        text49 = null;
        text50 = null;
        text51 = null;
        text52 = null;
        text53 = null;
        text54 = null;
        text55 = null;
        text56 = null;
        text57 = null;
        text58 = null;

        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        yesNo4 = null;
        yesNo5 = null;
        yesNo6 = null;
        yesNo7 = null;
        yesNo8 = null;
        yesNo9 = null;
        yesNo10 = null;
        yesNo11 = null;
        yesNo12 = null;
        yesNo13 = null;
        yesNo14 = null;
        yesNo15 = null;
        yesNo16 = null;
        yesNo17 = null;
        yesNo18 = null;
        yesNo19 = null;
        yesNo20 = null;
        yesNo21 = null;
        yesNo22 = null;
        yesNo23 = null;
        yesNo24 = null;
        yesNo25 = null;
        yesNo26 = null;
        yesNo27 = null;
        yesNo28 = null;
        yesNo29 = null;
        yesNo30 = null;
        yesNo31 = null;
        yesNo32 = null;
        yesNo33 = null;
        yesNo34 = null;
        yesNo35 = null;
        yesNo36 = null;
        yesNo37 = null;
        yesNo38 = null;
        yesNo39 = null;
        yesNo40 = null;
        yesNo41 = null;
        yesNo42 = null;
        yesNo43 = null;
        yesNo44 = null;
        yesNo45 = null;
        yesNo46 = null;
        yesNo47 = null;
        yesNo48 = null;
        yesNo49 = null;
        yesNo50 = null;
        yesNo51 = null;
        yesNo52 = null;
        yesNo53 = null;
        yesNo54 = null;
        yesNo55 = null;
        yesNo56 = null;
        yesNo57 = null;
        yesNo58 = null;
        yesNo59 = null;
        yesNo60 = null;
        yesNo61 = null;
        yesNo62 = null;
        yesNo63 = null;
        yesNo64 = null;
        yesNo65 = null;
        yesNo66 = null;
        yesNo67 = null;
        yesNo68 = null;
        yesNo69 = null;
        yesNo70 = null;
        yesNo71 = null;
        yesNo72 = null;
        yesNo73 = null;
        yesNo74 = null;
        yesNo75 = null;
        yesNo76 = null;
        yesNo77 = null;
        yesNo78 = null;
        yesNo79 = null;
        yesNo80 = null;
        yesNo81 = null;
        yesNo82 = null;

        number1 = null;
        number2 = null;
        number3 = null;
        number4 = null;
        number5 = null;
        number6 = null;
        number7 = null;
        number8 = null;
        number9 = null;
        number10 = null;
        number11 = null;
        number12 = null;
        number13 = null;
        number14 = null;
        number15 = null;
        number16 = null;
        number17 = null;
        number18 = null;
        number19 = null;
        number20 = null;

        agent1 = null;
        date1 = null;
        date1Precision = 1;

        remarks = null;

        taxons = new HashSet<Taxon>();
    }

    @Id
    @GeneratedValue
    @Column(name = "TaxonAttributeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getTaxonAttributeId() {
        return taxonAttributeId;
    }

    public void setTaxonAttributeId(Integer taxonAttributeId) {
        this.taxonAttributeId = taxonAttributeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId() {
        return this.taxonAttributeId;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "taxonAttribute")
    public Set<Taxon> getTaxons() {
        return taxons;
    }

    public void setTaxons(Set<Taxon> taxons) {
        this.taxons = taxons;
    }

    /**
     * @return
     */
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText3() {
        return text3;
    }

    /**
     * @param text3
     */
    public void setText3(String text3) {
        this.text3 = text3;
    }

    @Column(name = "Text4", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText4() {
        return text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    @Column(name = "Text5", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText5() {
        return text5;
    }

    public void setText5(String text5) {
        this.text5 = text5;
    }

    @Column(name = "Text6", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText6() {
        return text6;
    }

    public void setText6(String text6) {
        this.text6 = text6;
    }

    @Column(name = "Text7", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText7() {
        return text7;
    }

    public void setText7(String text7) {
        this.text7 = text7;
    }

    @Column(name = "Text8", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText8() {
        return text8;
    }

    public void setText8(String text8) {
        this.text8 = text8;
    }

    @Column(name = "Text9", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText9() {
        return text9;
    }

    public void setText9(String text9) {
        this.text9 = text9;
    }

    @Column(name = "Text10", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText10() {
        return text10;
    }

    public void setText10(String text10) {
        this.text10 = text10;
    }

    @Column(name = "Text11", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText11() {
        return text11;
    }

    public void setText11(String text11) {
        this.text11 = text11;
    }

    @Column(name = "Text12", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText12() {
        return text12;
    }

    public void setText12(String text12) {
        this.text12 = text12;
    }

    @Column(name = "Text13", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText13() {
        return text13;
    }

    public void setText13(String text13) {
        this.text13 = text13;
    }

    @Column(name = "Text14", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText14() {
        return text14;
    }

    public void setText14(String text14) {
        this.text14 = text14;
    }

    @Column(name = "Text15", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText15() {
        return text15;
    }

    public void setText15(String text15) {
        this.text15 = text15;
    }

    @Column(name = "Text16", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText16() {
        return text16;
    }

    public void setText16(String text16) {
        this.text16 = text16;
    }

    @Column(name = "Text17", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText17() {
        return text17;
    }

    public void setText17(String text17) {
        this.text17 = text17;
    }

    @Column(name = "Text18", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText18() {
        return text18;
    }

    public void setText18(String text18) {
        this.text18 = text18;
    }

    @Column(name = "Text19", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText19() {
        return text19;
    }

    public void setText19(String text19) {
        this.text19 = text19;
    }

    @Column(name = "Text20", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText20() {
        return text20;
    }

    public void setText20(String text20) {
        this.text20 = text20;
    }

    @Column(name = "Text21", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText21() {
        return text21;
    }

    public void setText21(String text21) {
        this.text21 = text21;
    }

    @Column(name = "Text22", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText22() {
        return text22;
    }

    public void setText22(String text22) {
        this.text22 = text22;
    }

    @Column(name = "Text23", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText23() {
        return text23;
    }

    public void setText23(String text23) {
        this.text23 = text23;
    }

    @Column(name = "Text24", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText24() {
        return text24;
    }

    public void setText24(String text24) {
        this.text24 = text24;
    }

    @Column(name = "Text25", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText25() {
        return text25;
    }

    public void setText25(String text25) {
        this.text25 = text25;
    }

    @Column(name = "Text26", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText26() {
        return text26;
    }

    public void setText26(String text26) {
        this.text26 = text26;
    }

    @Column(name = "Text27", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText27() {
        return text27;
    }

    public void setText27(String text27) {
        this.text27 = text27;
    }

    @Column(name = "Text28", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText28() {
        return text28;
    }

    public void setText28(String text28) {
        this.text28 = text28;
    }

    @Column(name = "Text29", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText29() {
        return text29;
    }

    public void setText29(String text29) {
        this.text29 = text29;
    }

    @Column(name = "Text30", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText30() {
        return text30;
    }

    public void setText30(String text30) {
        this.text30 = text30;
    }

    @Column(name = "Text31", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText31() {
        return text31;
    }

    public void setText31(String text31) {
        this.text31 = text31;
    }

    @Column(name = "Text32", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText32() {
        return text32;
    }

    public void setText32(String text32) {
        this.text32 = text32;
    }

    @Column(name = "Text33", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText33() {
        return text33;
    }

    public void setText33(String text33) {
        this.text33 = text33;
    }

    @Column(name = "Text34", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText34() {
        return text34;
    }

    public void setText34(String text34) {
        this.text34 = text34;
    }

    @Column(name = "Text35", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText35() {
        return text35;
    }

    public void setText35(String text35) {
        this.text35 = text35;
    }

    @Column(name = "Text36", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText36() {
        return text36;
    }

    public void setText36(String text36) {
        this.text36 = text36;
    }

    @Column(name = "Text37", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText37() {
        return text37;
    }

    public void setText37(String text37) {
        this.text37 = text37;
    }

    @Column(name = "Text38", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText38() {
        return text38;
    }

    public void setText38(String text38) {
        this.text38 = text38;
    }

    @Column(name = "Text39", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText39() {
        return text39;
    }

    public void setText39(String text39) {
        this.text39 = text39;
    }

    @Column(name = "Text40", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText40() {
        return text40;
    }

    public void setText40(String text40) {
        this.text40 = text40;
    }

    @Column(name = "Text41", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText41() {
        return text41;
    }

    public void setText41(String text41) {
        this.text41 = text41;
    }

    @Column(name = "Text42", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText42() {
        return text42;
    }

    public void setText42(String text42) {
        this.text42 = text42;
    }

    @Column(name = "Text43", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText43() {
        return text43;
    }

    public void setText43(String text43) {
        this.text43 = text43;
    }

    @Column(name = "Text44", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText44() {
        return text44;
    }

    public void setText44(String text44) {
        this.text44 = text44;
    }

    @Column(name = "Text45", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText45() {
        return text45;
    }

    public void setText45(String text45) {
        this.text45 = text45;
    }

    @Column(name = "Text46", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText46() {
        return text46;
    }

    public void setText46(String text46) {
        this.text46 = text46;
    }

    @Column(name = "Text47", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText47() {
        return text47;
    }

    public void setText47(String text47) {
        this.text47 = text47;
    }

    @Column(name = "Text48", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText48() {
        return text48;
    }

    public void setText48(String text48) {
        this.text48 = text48;
    }

    @Column(name = "Text49", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText49() {
        return text49;
    }

    public void setText49(String text49) {
        this.text49 = text49;
    }

    @Column(name = "Text50", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText50() {
        return text50;
    }

    public void setText50(String text50) {
        this.text50 = text50;
    }

    @Column(name = "Text51", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText51() {
        return text51;
    }

    public void setText51(String text51) {
        this.text51 = text51;
    }

    @Column(name = "Text52", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText52() {
        return text52;
    }

    public void setText52(String text52) {
        this.text52 = text52;
    }

    @Column(name = "Text53", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText53() {
        return text53;
    }

    public void setText53(String text53) {
        this.text53 = text53;
    }

    @Column(name = "Text54", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText54() {
        return text54;
    }

    public void setText54(String text54) {
        this.text54 = text54;
    }

    @Column(name = "Text55", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText55() {
        return text55;
    }

    public void setText55(String text55) {
        this.text55 = text55;
    }

    @Column(name = "Text56", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText56() {
        return text56;
    }

    public void setText56(String text56) {
        this.text56 = text56;
    }

    @Column(name = "Text57", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText57() {
        return text57;
    }

    public void setText57(String text57) {
        this.text57 = text57;
    }

    @Column(name = "Text58", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getText58() {
        return text58;
    }

    public void setText58(String text58) {
        this.text58 = text58;
    }

    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1() {
        return yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2() {
        return yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3() {
        return yesNo3;
    }

    public void setYesNo3(Boolean yesNo3) {
        this.yesNo3 = yesNo3;
    }

    @Column(name = "YesNo4", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo4() {
        return yesNo4;
    }

    public void setYesNo4(Boolean yesNo4) {
        this.yesNo4 = yesNo4;
    }

    @Column(name = "YesNo5", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo5() {
        return yesNo5;
    }

    public void setYesNo5(Boolean yesNo5) {
        this.yesNo5 = yesNo5;
    }

    @Column(name = "YesNo6", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo6() {
        return yesNo6;
    }

    public void setYesNo6(Boolean yesNo6) {
        this.yesNo6 = yesNo6;
    }

    @Column(name = "YesNo7", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo7() {
        return yesNo7;
    }

    public void setYesNo7(Boolean yesNo7) {
        this.yesNo7 = yesNo7;
    }

    @Column(name = "YesNo8", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo8() {
        return yesNo8;
    }

    public void setYesNo8(Boolean yesNo8) {
        this.yesNo8 = yesNo8;
    }

    @Column(name = "YesNo9", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo9() {
        return yesNo9;
    }

    public void setYesNo9(Boolean yesNo9) {
        this.yesNo9 = yesNo9;
    }

    @Column(name = "YesNo10", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo10() {
        return yesNo10;
    }

    public void setYesNo10(Boolean yesNo10) {
        this.yesNo10 = yesNo10;
    }

    @Column(name = "YesNo11", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo11() {
        return yesNo11;
    }

    public void setYesNo11(Boolean yesNo11) {
        this.yesNo11 = yesNo11;
    }

    @Column(name = "YesNo12", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo12() {
        return yesNo12;
    }

    public void setYesNo12(Boolean yesNo12) {
        this.yesNo12 = yesNo12;
    }

    @Column(name = "YesNo13", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo13() {
        return yesNo13;
    }

    public void setYesNo13(Boolean yesNo13) {
        this.yesNo13 = yesNo13;
    }

    @Column(name = "YesNo14", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo14() {
        return yesNo14;
    }

    public void setYesNo14(Boolean yesNo14) {
        this.yesNo14 = yesNo14;
    }

    @Column(name = "YesNo15", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo15() {
        return yesNo15;
    }

    public void setYesNo15(Boolean yesNo15) {
        this.yesNo15 = yesNo15;
    }

    @Column(name = "YesNo16", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo16() {
        return yesNo16;
    }

    public void setYesNo16(Boolean yesNo16) {
        this.yesNo16 = yesNo16;
    }

    @Column(name = "YesNo17", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo17() {
        return yesNo17;
    }

    public void setYesNo17(Boolean yesNo17) {
        this.yesNo17 = yesNo17;
    }

    @Column(name = "YesNo18", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo18() {
        return yesNo18;
    }

    public void setYesNo18(Boolean yesNo18) {
        this.yesNo18 = yesNo18;
    }

    @Column(name = "YesNo19", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo19() {
        return yesNo19;
    }

    public void setYesNo19(Boolean yesNo19) {
        this.yesNo19 = yesNo19;
    }

    @Column(name = "YesNo20", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo20() {
        return yesNo20;
    }

    public void setYesNo20(Boolean yesNo20) {
        this.yesNo20 = yesNo20;
    }

    @Column(name = "YesNo21", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo21() {
        return yesNo21;
    }

    public void setYesNo21(Boolean yesNo21) {
        this.yesNo21 = yesNo21;
    }

    @Column(name = "YesNo22", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo22() {
        return yesNo22;
    }

    public void setYesNo22(Boolean yesNo22) {
        this.yesNo22 = yesNo22;
    }

    @Column(name = "YesNo23", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo23() {
        return yesNo23;
    }

    public void setYesNo23(Boolean yesNo23) {
        this.yesNo23 = yesNo23;
    }

    @Column(name = "YesNo24", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo24() {
        return yesNo24;
    }

    public void setYesNo24(Boolean yesNo24) {
        this.yesNo24 = yesNo24;
    }

    @Column(name = "YesNo25", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo25() {
        return yesNo25;
    }

    public void setYesNo25(Boolean yesNo25) {
        this.yesNo25 = yesNo25;
    }

    @Column(name = "YesNo26", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo26() {
        return yesNo26;
    }

    public void setYesNo26(Boolean yesNo26) {
        this.yesNo26 = yesNo26;
    }

    @Column(name = "YesNo27", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo27() {
        return yesNo27;
    }

    public void setYesNo27(Boolean yesNo27) {
        this.yesNo27 = yesNo27;
    }

    @Column(name = "YesNo28", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo28() {
        return yesNo28;
    }

    public void setYesNo28(Boolean yesNo28) {
        this.yesNo28 = yesNo28;
    }

    @Column(name = "YesNo29", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo29() {
        return yesNo29;
    }

    public void setYesNo29(Boolean yesNo29) {
        this.yesNo29 = yesNo29;
    }

    @Column(name = "YesNo30", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo30() {
        return yesNo30;
    }

    public void setYesNo30(Boolean yesNo30) {
        this.yesNo30 = yesNo30;
    }

    @Column(name = "YesNo31", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo31() {
        return yesNo31;
    }

    public void setYesNo31(Boolean yesNo31) {
        this.yesNo31 = yesNo31;
    }

    @Column(name = "YesNo32", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo32() {
        return yesNo32;
    }

    public void setYesNo32(Boolean yesNo32) {
        this.yesNo32 = yesNo32;
    }

    @Column(name = "YesNo33", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo33() {
        return yesNo33;
    }

    public void setYesNo33(Boolean yesNo33) {
        this.yesNo33 = yesNo33;
    }

    @Column(name = "YesNo34", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo34() {
        return yesNo34;
    }

    public void setYesNo34(Boolean yesNo34) {
        this.yesNo34 = yesNo34;
    }

    @Column(name = "YesNo35", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo35() {
        return yesNo35;
    }

    public void setYesNo35(Boolean yesNo35) {
        this.yesNo35 = yesNo35;
    }

    @Column(name = "YesNo36", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo36() {
        return yesNo36;
    }

    public void setYesNo36(Boolean yesNo36) {
        this.yesNo36 = yesNo36;
    }

    @Column(name = "YesNo37", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo37() {
        return yesNo37;
    }

    public void setYesNo37(Boolean yesNo37) {
        this.yesNo37 = yesNo37;
    }

    @Column(name = "YesNo38", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo38() {
        return yesNo38;
    }

    public void setYesNo38(Boolean yesNo38) {
        this.yesNo38 = yesNo38;
    }

    @Column(name = "YesNo39", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo39() {
        return yesNo39;
    }

    public void setYesNo39(Boolean yesNo39) {
        this.yesNo39 = yesNo39;
    }

    @Column(name = "YesNo40", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo40() {
        return yesNo40;
    }

    public void setYesNo40(Boolean yesNo40) {
        this.yesNo40 = yesNo40;
    }

    @Column(name = "YesNo41", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo41() {
        return yesNo41;
    }

    public void setYesNo41(Boolean yesNo41) {
        this.yesNo41 = yesNo41;
    }

    @Column(name = "YesNo42", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo42() {
        return yesNo42;
    }

    public void setYesNo42(Boolean yesNo42) {
        this.yesNo42 = yesNo42;
    }

    @Column(name = "YesNo43", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo43() {
        return yesNo43;
    }

    public void setYesNo43(Boolean yesNo43) {
        this.yesNo43 = yesNo43;
    }

    @Column(name = "YesNo44", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo44() {
        return yesNo44;
    }

    public void setYesNo44(Boolean yesNo44) {
        this.yesNo44 = yesNo44;
    }

    @Column(name = "YesNo45", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo45() {
        return yesNo45;
    }

    public void setYesNo45(Boolean yesNo45) {
        this.yesNo45 = yesNo45;
    }

    @Column(name = "YesNo46", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo46() {
        return yesNo46;
    }

    public void setYesNo46(Boolean yesNo46) {
        this.yesNo46 = yesNo46;
    }

    @Column(name = "YesNo47", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo47() {
        return yesNo47;
    }

    public void setYesNo47(Boolean yesNo47) {
        this.yesNo47 = yesNo47;
    }

    @Column(name = "YesNo48", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo48() {
        return yesNo48;
    }

    public void setYesNo48(Boolean yesNo48) {
        this.yesNo48 = yesNo48;
    }

    @Column(name = "YesNo49", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo49() {
        return yesNo49;
    }

    public void setYesNo49(Boolean yesNo49) {
        this.yesNo49 = yesNo49;
    }

    @Column(name = "YesNo50", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo50() {
        return yesNo50;
    }

    public void setYesNo50(Boolean yesNo50) {
        this.yesNo50 = yesNo50;
    }

    @Column(name = "YesNo51", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo51() {
        return yesNo51;
    }

    public void setYesNo51(Boolean yesNo51) {
        this.yesNo51 = yesNo51;
    }

    @Column(name = "YesNo52", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo52() {
        return yesNo52;
    }

    public void setYesNo52(Boolean yesNo52) {
        this.yesNo52 = yesNo52;
    }

    @Column(name = "YesNo53", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo53() {
        return yesNo53;
    }

    public void setYesNo53(Boolean yesNo53) {
        this.yesNo53 = yesNo53;
    }

    @Column(name = "YesNo54", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo54() {
        return yesNo54;
    }

    public void setYesNo54(Boolean yesNo54) {
        this.yesNo54 = yesNo54;
    }

    @Column(name = "YesNo55", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo55() {
        return yesNo55;
    }

    public void setYesNo55(Boolean yesNo55) {
        this.yesNo55 = yesNo55;
    }

    @Column(name = "YesNo56", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo56() {
        return yesNo56;
    }

    public void setYesNo56(Boolean yesNo56) {
        this.yesNo56 = yesNo56;
    }

    @Column(name = "YesNo57", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo57() {
        return yesNo57;
    }

    public void setYesNo57(Boolean yesNo57) {
        this.yesNo57 = yesNo57;
    }

    @Column(name = "YesNo58", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo58() {
        return yesNo58;
    }

    public void setYesNo58(Boolean yesNo58) {
        this.yesNo58 = yesNo58;
    }

    @Column(name = "YesNo59", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo59() {
        return yesNo59;
    }

    public void setYesNo59(Boolean yesNo59) {
        this.yesNo59 = yesNo59;
    }

    @Column(name = "YesNo60", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo60() {
        return yesNo60;
    }

    public void setYesNo60(Boolean yesNo60) {
        this.yesNo60 = yesNo60;
    }

    @Column(name = "YesNo61", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo61() {
        return yesNo61;
    }

    public void setYesNo61(Boolean yesNo61) {
        this.yesNo61 = yesNo61;
    }

    @Column(name = "YesNo62", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo62() {
        return yesNo62;
    }

    public void setYesNo62(Boolean yesNo62) {
        this.yesNo62 = yesNo62;
    }

    @Column(name = "YesNo63", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo63() {
        return yesNo63;
    }

    public void setYesNo63(Boolean yesNo63) {
        this.yesNo63 = yesNo63;
    }

    @Column(name = "YesNo64", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo64() {
        return yesNo64;
    }

    public void setYesNo64(Boolean yesNo64) {
        this.yesNo64 = yesNo64;
    }

    @Column(name = "YesNo65", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo65() {
        return yesNo65;
    }

    public void setYesNo65(Boolean yesNo65) {
        this.yesNo65 = yesNo65;
    }

    @Column(name = "YesNo66", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo66() {
        return yesNo66;
    }

    public void setYesNo66(Boolean yesNo66) {
        this.yesNo66 = yesNo66;
    }

    @Column(name = "YesNo67", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo67() {
        return yesNo67;
    }

    public void setYesNo67(Boolean yesNo67) {
        this.yesNo67 = yesNo67;
    }

    @Column(name = "YesNo68", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo68() {
        return yesNo68;
    }

    public void setYesNo68(Boolean yesNo68) {
        this.yesNo68 = yesNo68;
    }

    @Column(name = "YesNo69", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo69() {
        return yesNo69;
    }

    public void setYesNo69(Boolean yesNo69) {
        this.yesNo69 = yesNo69;
    }

    @Column(name = "YesNo70", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo70() {
        return yesNo70;
    }

    public void setYesNo70(Boolean yesNo70) {
        this.yesNo70 = yesNo70;
    }

    @Column(name = "YesNo71", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo71() {
        return yesNo71;
    }

    public void setYesNo71(Boolean yesNo71) {
        this.yesNo71 = yesNo71;
    }

    @Column(name = "YesNo72", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo72() {
        return yesNo72;
    }

    public void setYesNo72(Boolean yesNo72) {
        this.yesNo72 = yesNo72;
    }

    @Column(name = "YesNo73", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo73() {
        return yesNo73;
    }

    public void setYesNo73(Boolean yesNo73) {
        this.yesNo73 = yesNo73;
    }

    @Column(name = "YesNo74", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo74() {
        return yesNo74;
    }

    public void setYesNo74(Boolean yesNo74) {
        this.yesNo74 = yesNo74;
    }

    @Column(name = "YesNo75", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo75() {
        return yesNo75;
    }

    public void setYesNo75(Boolean yesNo75) {
        this.yesNo75 = yesNo75;
    }

    @Column(name = "YesNo76", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo76() {
        return yesNo76;
    }

    public void setYesNo76(Boolean yesNo76) {
        this.yesNo76 = yesNo76;
    }

    @Column(name = "YesNo77", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo77() {
        return yesNo77;
    }

    public void setYesNo77(Boolean yesNo77) {
        this.yesNo77 = yesNo77;
    }

    @Column(name = "YesNo78", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo78() {
        return yesNo78;
    }

    public void setYesNo78(Boolean yesNo78) {
        this.yesNo78 = yesNo78;
    }

    @Column(name = "YesNo79", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo79() {
        return yesNo79;
    }

    public void setYesNo79(Boolean yesNo79) {
        this.yesNo79 = yesNo79;
    }

    @Column(name = "YesNo80", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo80() {
        return yesNo80;
    }

    public void setYesNo80(Boolean yesNo80) {
        this.yesNo80 = yesNo80;
    }

    @Column(name = "YesNo81", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo81() {
        return yesNo81;
    }

    public void setYesNo81(Boolean yesNo81) {
        this.yesNo81 = yesNo81;
    }

    @Column(name = "YesNo82", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo82() {
        return yesNo82;
    }

    public void setYesNo82(Boolean yesNo82) {
        this.yesNo82 = yesNo82;
    }

    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1() {
        return number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2() {
        return number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber3() {
        return number3;
    }

    public void setNumber3(Float number3) {
        this.number3 = number3;
    }

    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber4() {
        return number4;
    }

    public void setNumber4(Float number4) {
        this.number4 = number4;
    }

    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber5() {
        return number5;
    }

    public void setNumber5(Float number5) {
        this.number5 = number5;
    }

    @Column(name = "Number6", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber6() {
        return number6;
    }

    public void setNumber6(Float number6) {
        this.number6 = number6;
    }

    @Column(name = "Number7", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber7() {
        return number7;
    }

    public void setNumber7(Float number7) {
        this.number7 = number7;
    }

    @Column(name = "Number8", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber8() {
        return number8;
    }

    public void setNumber8(Float number8) {
        this.number8 = number8;
    }

    @Column(name = "Number9", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber9() {
        return number9;
    }

    public void setNumber9(Float number9) {
        this.number9 = number9;
    }

    @Column(name = "Number10", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber10() {
        return number10;
    }

    public void setNumber10(Float number10) {
        this.number10 = number10;
    }

    @Column(name = "Number11", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber11() {
        return number11;
    }

    public void setNumber11(Float number11) {
        this.number11 = number11;
    }

    @Column(name = "Number12", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber12() {
        return number12;
    }

    public void setNumber12(Float number12) {
        this.number12 = number12;
    }

    @Column(name = "Number13", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber13() {
        return number13;
    }

    public void setNumber13(Float number13) {
        this.number13 = number13;
    }

    @Column(name = "Number14", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber14() {
        return number14;
    }

    public void setNumber14(Float number14) {
        this.number14 = number14;
    }

    @Column(name = "Number15", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber15() {
        return number15;
    }

    public void setNumber15(Float number15) {
        this.number15 = number15;
    }

    @Column(name = "Number16", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber16() {
        return number16;
    }

    public void setNumber16(Float number16) {
        this.number16 = number16;
    }

    @Column(name = "Number17", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber17() {
        return number17;
    }

    public void setNumber17(Float number17) {
        this.number17 = number17;
    }

    @Column(name = "Number18", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber18() {
        return number18;
    }

    public void setNumber18(Float number18) {
        this.number18 = number18;
    }

    @Column(name = "Number19", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber19() {
        return number19;
    }

    public void setNumber19(Integer number19) {
        this.number19 = number19;
    }

    @Column(name = "Number20", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber20() {
        return number20;
    }

    public void setNumber20(Integer number20) {
        this.number20 = number20;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent1ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent1() {
        return agent1;
    }

    public void setAgent1(Agent agent1) {
        this.agent1 = agent1;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    public void setDate1(Calendar date1) {
        this.date1 = date1;
    }

    @Column(name = "Date1Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate1Precision() {
        return date1Precision;
    }

    public void setDate1Precision(Byte date1Precision) {
        this.date1Precision = date1Precision;
    }

    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        TaxonAttribute obj = (TaxonAttribute) super.clone();
        obj.taxonAttributeId = null;
        obj.taxons = new HashSet<Taxon>();

        return obj;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle() {
        return toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString() {
        String str = DataObjFieldFormatMgr.getInstance().format(this, getDataClass());
        return StringUtils.isNotEmpty(str) ? str : "1";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId() {
        return Taxon.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId() {
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT TaxonID FROM taxon WHERE TaxonAttributeID = " + taxonAttributeId);
        if (ids.size() == 1) {
            return (Integer) ids.get(0);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass() {
        return TaxonAttribute.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId() {
        return getClassTableId();
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId() {
        return 93;
    }

    /**
     * @param o
     * @return true if 'non-system' fields all match.
     */
    public boolean matches(TaxonAttribute o) {
        if (o == null) {
            return false;
        }

        return ((text1 == null && o.text1 == null) || ((text1 != null && o.text1 != null) && text1.equals(o.text1))) &&
                ((text2 == null && o.text2 == null) || ((text2 != null && o.text2 != null) && text2.equals(o.text2))) &&
                ((text3 == null && o.text3 == null) || ((text3 != null && o.text3 != null) && text3.equals(o.text3))) &&
                ((yesNo1 == null && o.yesNo1 == null) || ((yesNo1 != null && o.yesNo1 != null) && yesNo1.equals(o.yesNo1))) &&
                ((yesNo2 == null && o.yesNo2 == null) || ((yesNo2 != null && o.yesNo2 != null) && yesNo2.equals(o.yesNo2))) &&
                ((yesNo3 == null && o.yesNo3 == null) || ((yesNo3 != null && o.yesNo3 != null) && yesNo3.equals(o.yesNo3)));
    }
}
