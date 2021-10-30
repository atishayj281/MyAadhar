package android.example.myaadhar.models;


import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "OtpResult")
@XmlEnum
public enum OtpResult {

    @XmlEnumValue("y")
    Y("y"), @XmlEnumValue("n")
    N("n");
    private final String value;

    OtpResult(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OtpResult fromValue(String v) {
        for (OtpResult c : OtpResult.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

