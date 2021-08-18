!define PRD_VERSION   1.5.1.1 # Synchronize it with 'GUIMain.java'
!define APP_VERSION   1.5.1   # ---

VIProductVersion      "${PRD_VERSION}"
VIAddVersionKey       "FileDescription" "VMA Solutions Setup"
VIAddVersionKey       "FileVersion"     "${APP_VERSION}"
VIAddVersionKey       "ProductName"     "VMA Solutions"
VIAddVersionKey       "ProductVersion"  "${APP_VERSION}"
VIAddVersionKey       "LegalCopyright"  "Copyright (C) VMA Consultant"
VIAddVersionKey       "CompanyName"     "VMA Consultant"

Name                  "VMA Solutions(tm) ${APP_VERSION}"
OutFile               "vma-installer.exe"
InstallDir            "$PROGRAMFILES\vma"
SetCompressor         /SOLID /FINAL zlib
RequestExecutionLevel admin

##### Install
section
    SetShellVarContext  all
    SetOutPath         "$INSTDIR"
    File               "vma.jar"
    File               "vma.ico"
    WriteUninstaller   "$INSTDIR\uninstall.exe"
    CreateDirectory    "$SMPROGRAMS\VMA Solutions"
    CreateShortCut     "$SMPROGRAMS\VMA Solutions\VMA Solutions.lnk" "$INSTDIR\vma.jar"       "" "$INSTDIR\vma.ico"
    CreateShortCut     "$SMPROGRAMS\VMA Solutions\Uninstall.lnk"     "$INSTDIR\uninstall.exe"
sectionEnd

##### Uninstall
section "uninstall"
    SetShellVarContext all
    Delete             "$INSTDIR\vma.jar"
    Delete             "$INSTDIR\vma.ico"
    Delete             "$INSTDIR\uninstall.exe"
    RMDir              "$INSTDIR"
    Delete             "$SMPROGRAMS\VMA Solutions\VMA Solutions.lnk"
    Delete             "$SMPROGRAMS\VMA Solutions\Uninstall.lnk"
    RMDir              "$SMPROGRAMS\VMA Solutions"
sectionEnd

##### Callback to be called when the installer is nearly finished initializing
Function .OnInit
    # Check if the current user has an administrative privileges
        UserInfo::GetAccountType
        Pop $0
        StrCmp $0 "Admin" +3
        MessageBox MB_OK "This installer needs to be run by a user with an administrative privileges."
        Abort

    # Check if the needed JRE is installed in the system
        System::Call "kernel32::GetCurrentProcess() i .s"
        System::Call "kernel32::IsWow64Process(i s, *i .r0)"
        IntCmp $0 0 +5
        SetRegView 64
        Call GetJavaVersion
        SetRegView 32
        Goto +2
        Call GetJavaVersion
        Pop $0
        Pop $1
        Pop $2
        Pop $3
        StrCmp $0 "no" L_JRENotOK
        IntCmp $0 1 +1 L_JRENotOK
        IntCmp $1 6 +1 L_JRENotOK
        Goto L_ConfirmInstallation
    L_JRENotOK:
        MessageBox MB_YESNO "The installer cannot detect the presence of Java Runtime Enviroment (JRE) version 1.6 or newer \
                             in the system. VMA needs JRE version 1.6 or newer to run.$\n$\nIt is possible that the needed \
                             JRE is actually installed in the system but cannot be detected by the installer.$\n$\nDo you \
                             wish to continue?" IDYES +2
        Abort

    # Confirm installation
    L_ConfirmInstallation:
        MessageBox MB_YESNO "This installer will install VMA Solutions ${APP_VERSION}.$\n$\n\
                             Do you wish to continue?" IDYES +2
        Abort
FunctionEnd

##### Callback to be called when the uninstaller is nearly finished initializing
Function un.onInit
    MessageBox MB_YESNO "Do you really want to uninstall VMA Solutions ${APP_VERSION}?" IDYES +2
    Abort
FunctionEnd

###############################################################################
; http://nsis.sourceforge.net/Get_full_Java_version
; Find installed java version and return major, minor, micro and build/update version
; For some reason v1.2.1_004 did not give a build version, but it's the only one of its kind.
; There are 3 ways to get the build version:
;     1) from the UpdateVersion key
;     2) or from the MicroVersion key
;     3) or from the JavaHome key
; Example:
;      call GetJavaVersion
;      pop $0 ; major version
;      pop $1 ; minor version
;      pop $2 ; micro version
;      pop $3 ; build/update version
;      strcmp $0 "no" JavaNotInstalled
;      strcmp $3 "" nobuild
;      DetailPrint "$0.$1.$2_$3"
;      goto fin
;  nobuild:
;      DetailPrint "$0.$1.$2"
;      goto fin
;  JavaNotInstalled:
;      DetailPrint "Java Not Installed"
;  fin:
Function GetJavaVersion
        push $R0
        push $R1
        push $2
        push $0
        push $3
        push $4

        ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
        StrCmp $2 "" DetectTry2
        ReadRegStr $3 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$2" "MicroVersion"
        StrCmp $3 "" DetectTry2
        ReadRegStr $4 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$2" "UpdateVersion"
        StrCmp $4 "" 0 GotFromUpdate
        ReadRegStr $4 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$2" "JavaHome"
        Goto GotJRE
    DetectTry2:
        ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
        StrCmp $2 "" NoFound
        ReadRegStr $3 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$2" "MicroVersion"
        StrCmp $3 "" NoFound
        ReadRegStr $4 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$2" "UpdateVersion"
        StrCmp $4 "" 0 GotFromUpdate
        ReadRegStr $4 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$2" "JavaHome"
    GotJRE:
        ; calc build version
        strlen $0 $3
        intcmp $0 1 0 0 GetFromMicro
        ; get it from the path
    GetFromPath:
        strlen $R0 $4
        intop $R0 $R0 - 1
        StrCpy $0 ""
    loopP:
        StrCpy $R1 $4 1 $R0
        StrCmp $R1 "" DotFoundP
        StrCmp $R1 "_" UScoreFound
        StrCmp $R1 "." DotFoundP
        StrCpy $0 "$R1$0"
        Goto GoLoopingP
    DotFoundP:
        push ""
        Exch 6
        goto CalcMicro
    UScoreFound:
        push $0
        Exch 6
        goto CalcMicro
    GoLoopingP:
        intcmp $R0 0 DotFoundP DotFoundP
        IntOp $R0 $R0 - 1
        Goto loopP
    GetFromMicro:
        strcpy $4 $3
        goto GetFromPath
    GotFromUpdate:
        push $4
        Exch 6

    CalcMicro:
        Push $3 ; micro
        Exch 6
        ; break version into major and minor
        StrCpy $R0 0
        StrCpy $0 ""
    loop:
        StrCpy $R1 $2 1 $R0
        StrCmp $R1 "" done
        StrCmp $R1 "." DotFound
        StrCpy $0 "$0$R1"
        Goto GoLooping
    DotFound:
        Push $0 ; major
        Exch 5
        StrCpy $0 ""
    GoLooping:
        IntOp $R0 $R0 + 1
        Goto loop

    done:
        Push $0 ; minor
        Exch 7
        ; restore register values
        pop $0
        pop $2
        pop $R1
        pop $R0
        pop $3
        pop $4
        return
    NoFound:
        pop $4
        pop $3
        pop $0
        pop $2
        pop $R1
        pop $R0
        push ""
        push "installed"
        push "java"
        push "no"
FunctionEnd
