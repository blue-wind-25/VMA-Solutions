-injars       vma.jar
-outjars      vma_x.jar
-printmapping vma.map
-libraryjars  <java.home>/lib/rt.jar

-keep public class appwrapper.AppMain {
    public static void main(java.lang.String[]);
}
-keep public class anemonesoft.gui.GUIMain

-optimizationpasses 3
-overloadaggressively
-allowaccessmodification
-repackageclasses ''
