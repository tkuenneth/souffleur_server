module com.thomaskuenneth.souffleur.server {
    requires jdk.httpserver;
    requires java.desktop;
    requires java.logging;
    requires org.json;
    requires com.google.zxing;

    exports eu.thomaskuenneth.souffleur;
}