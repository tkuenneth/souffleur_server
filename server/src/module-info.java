module eu.thomaskuenneth.souffleur.server {
    requires jdk.httpserver;
    requires java.desktop;
    requires java.logging;
    requires org.json;
    requires com.google.zxing;
    requires java.prefs;

    exports eu.thomaskuenneth.souffleur;
}