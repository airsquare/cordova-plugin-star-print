//
//  AppDelegate.h
//  IOS_SDK
//
//  Created by satsuki on 12/07/13.
//  Copyright 2012 - 2013 STAR MICRONICS CO., LTD. All rights reserved.
//
typedef enum _SMPrinterType {
    SMPrinterTypeUnknown = 0,
    SMPrinterTypeDesktopPrinterStarLine,
    SMPrinterTypePortablePrinterStarLine,
    SMPrinterTypePortablePrinterESCPOS
} SMPrinterType;

typedef enum _SMPaperWidth {
    SMPaperWidth2inch,
    SMPaperWidth3inch,
    SMPaperWidth4inch
} SMPaperWidth;

typedef enum _SMLanguage {
    SMLanguageEnglish,
    SMLanguageEnglishUtf8,
    SMLanguageFrench,
    SMLanguageFrenchUtf8,
    SMLanguagePortuguese,
    SMLanguagePortugueseUtf8,
    SMLanguageSpanish,
    SMLanguageSpanishUtf8,
    SMLanguageRussian,
    SMLanguageRussianUtf8,
    SMLanguageJapanese,
    SMLanguageJapaneseUtf8,
    SMLanguageSimplifiedChinese,
    SMLanguageSimplifiedChineseUtf8,
    SMLanguageTraditionalChinese,
    SMLanguageTraditionalChineseUtf8,
} SMLanguage;

+ (NSString *)getPortName;
+ (void)setPortName:(NSString *)m_portName;
+ (NSString*)getPortSettings;
+ (void)setPortSettings:(NSString *)m_portSettings;

+ (NSString *)getDrawerPortName;
+ (void)setDrawerPortName:(NSString *)portName;

+ (void)setButtonArrayAsOldStyle:(NSArray *)buttons;

+ (SMPrinterType)parsePortSettings:(NSString *)portSettings;

+ (NSString *)HTMLCSS;

@end
