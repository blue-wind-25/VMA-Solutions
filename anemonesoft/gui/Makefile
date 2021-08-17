# Copyright (C) 2010-2011 Aloysius Indrayanto
#                         AnemoneSoft.com

# anemonesoft/gui/Makefile

include ../../Makefile.config
include ../../Makefile.utils

CLEAN_DIRS = dialog toolbox component control tab

CLS_COMMON     = GUIMain.class \
                 GUtil.class

CLS_DIALOG_BOX = dialog/AboutBox.class              \
                 dialog/RefsBox.class               \
                 dialog/FileSelectorBox.class       \
                 dialog/MultiFileSelectorBox.class  \
                 dialog/ResolutionSelectorBox.class \
                 dialog/SymbolSelectorBox.class     \
                 dialog/ImageBox.class              \
                 dialog/HelpBox.class               \
                 dialog/PBDesignParam.class         \
                 dialog/PrintPreview.class

CLS_TOOL_BOX   = toolbox/StudentTTable.class       \
                 toolbox/FisherFTable.class        \
                 toolbox/GrubbsCTable.class        \
                 toolbox/CalcPMC.class             \
                 toolbox/CalcTI.class              \
                 toolbox/CalcCI.class              \
                 toolbox/CalcND.class              \
                 toolbox/CalcDLQL.class            \
                 toolbox/COWAndPeakDetection.class

CLS_COMPONENT  = component/Saveable.class           \
                 component/NumericDocument.class    \
                 component/SheetRangeDocument.class \
                 component/FlexGridLayout.class     \
                 component/TabButtonComponent.class \
                 component/LineNumberTable.class    \
                 component/Spreadsheet.class        \
                 component/ColorBoxRenderer.class

CLS_CONTROL    = control/AccordionPanel.class               \
                 control/ResultPanel.class                  \
                 control/StdAnalysisSettingPanel.class      \
                 control/StdPlotCaptionSettingPanel.class   \
                 control/StdPlotDataRangeSettingPanel.class \
                 control/StdPlotDataRangeCaptionPanel.class \
                 control/StdPlotDataValuePanel.class        \
                 control/StdPlotDataPointSettingPanel.class \
                 control/StdPlotAxisScaleSettingPanel.class \
                 control/StdPlotMiscSettingPanel.class

CLS_TAB_MODULE = tab/SpreadsheetPanel.class     \
                 tab/ScatterLinePlotPanel.class \
                 tab/BoxWhiskerPlotPanel.class  \
                 tab/FirstOrderRegPanel.class   \
                 tab/SecondOrderRegPanel.class  \
                 tab/MandelTestPanel.class      \
                 tab/HomogeneityPanel.class     \
                 tab/AccuracyPanel.class        \
                 tab/APGonzalesPanel.class      \
                 tab/APRozetPanel.class         \
                 tab/PrecisionRSDPanel.class    \
                 tab/PrecisionOWANOPanel.class  \
                 tab/RobustnessPanel.class      \
                 tab/QCShewhartPanel.class      \
                 tab/ZScorePanel.class          \
                 tab/ZFactorPanel.class         \
                 tab/GrubbsTestPanel.class

CLASSPATH  = ../..
LIB_SOURCE = $(CLS_COMMON) $(CLS_DIALOG_BOX) $(CLS_TOOL_BOX) $(CLS_COMPONENT) $(CLS_CONTROL) $(CLS_TAB_MODULE)

default: $(LIB_SOURCE)
	@echo > /dev/null

