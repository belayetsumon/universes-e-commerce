/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.ecommerce.app.globalServices;

/**
 *
 * @author libertyerp_local
 */
public enum District {
    // Dhaka Division
    DHAKA("Dhaka"),
    GAZIPUR("Gazipur"),
    KISHOREGANJ("Kishoreganj"),
    MANIKGANJ("Manikganj"),
    MUNSHIGANJ("Munshiganj"),
    NARAYANGANJ("Narayanganj"),
    NARSINGDI("Narsingdi"),
    RAJBARI("Rajbari"),
    SHARIATPUR("Shariatpur"),
    TANGAIL("Tangail"),
    FARIDPUR("Faridpur"),
    GOPALGANJ("Gopalganj"),
    MADARIPUR("Madaripur"),
    // Chattogram Division
    CHATTOGRAM("Chattogram"),
    COXS_BAZAR("Cox's Bazar"),
    FENI("Feni"),
    KHAGRACHHARI("Khagrachhari"),
    LAKSHMIPUR("Lakshmipur"),
    NOAKHALI("Noakhali"),
    RANGAMATI("Rangamati"),
    BANDARBAN("Bandarban"),
    BRAHMANBARIA("Brahmanbaria"),
    CHANDPUR("Chandpur"),
    COMILLA("Cumilla"),
    // Khulna Division
    BAGERHAT("Bagerhat"),
    CHUADANGA("Chuadanga"),
    JASHORE("Jashore"),
    JHENAIDAH("Jhenaidah"),
    KHULNA("Khulna"),
    KUSHTIA("Kushtia"),
    MAGURA("Magura"),
    MEHERPUR("Meherpur"),
    NARAIL("Narail"),
    SATKHIRA("Satkhira"),
    // Rajshahi Division
    BOGURA("Bogura"),
    CHAPAINAWABGANJ("Chapainawabganj"),
    JOYPURHAT("Joypurhat"),
    NAOGAON("Naogaon"),
    NATORE("Natore"),
    PABNA("Pabna"),
    RAJSHAHI("Rajshahi"),
    SIRAJGANJ("Sirajganj"),
    // Rangpur Division
    DINAJPUR("Dinajpur"),
    GAIBANDHA("Gaibandha"),
    KURIGRAM("Kurigram"),
    LALMONIRHAT("Lalmonirhat"),
    NILPHAMARI("Nilphamari"),
    PANCHAGARH("Panchagarh"),
    RANGPUR("Rangpur"),
    THAKURGAON("Thakurgaon"),
    // Sylhet Division
    HABIGANJ("Habiganj"),
    MAULVIBAZAR("Moulvibazar"),
    SUNAMGANJ("Sunamganj"),
    SYLHET("Sylhet"),
    // Barishal Division
    BARGUNA("Barguna"),
    BARISHAL("Barishal"),
    BHOLA("Bhola"),
    JHALOKATI("Jhalokati"),
    PATUAKHALI("Patuakhali"),
    PIROJPUR("Pirojpur"),
    // Mymensingh Division
    JAMALPUR("Jamalpur"),
    MYMENSINGH("Mymensingh"),
    NETRAKONA("Netrokona"),
    SHERPUR("Sherpur");
    private final String displayName;

    District(String displayName
    ) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
