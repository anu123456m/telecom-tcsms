package com.amdocs.telecom.main;

import com.amdocs.telecom.security.CaptchaGenerator;
import com.amdocs.telecom.security.OTPService;
import com.amdocs.telecom.security.PasswordUtil;

public class SecurityCheck {
    public static void main(String[] args) {
        String hash = PasswordUtil.hash("Amu@12345");
        System.out.println("Hash          : " + hash);
        System.out.println("Right password: " + PasswordUtil.verify("Amu@12345", hash));
        System.out.println("Wrong password: " + PasswordUtil.verify("amu@12345", hash));
        System.out.println("Strong 'abc'       : " + PasswordUtil.isStrong("abc"));
        System.out.println("Strong 'Amu@12345' : " + PasswordUtil.isStrong("Amu@12345"));

        String captcha = CaptchaGenerator.generate();
        System.out.println("CAPTCHA       : " + captcha);
        System.out.println("Verify (lower): " + CaptchaGenerator.verify(captcha, captcha.toLowerCase()));

        OTPService otp = new OTPService();
        String code = otp.generateOtp();
        System.out.println("OTP           : " + code);
        System.out.println("OTP matches   : " + otp.matches(code, code));
        System.out.println("OTP expired?  : " + otp.isExpired(otp.expiryFromNow()));
    }
}