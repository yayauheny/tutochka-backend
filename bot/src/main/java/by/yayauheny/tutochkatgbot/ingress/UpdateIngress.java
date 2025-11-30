package by.yayauheny.tutochkatgbot.ingress;

/**
 * Interface for receiving Telegram updates
 */
public interface UpdateIngress {
    
    /**
     * Start receiving updates
     */
    void start();
    
    /**
     * Stop receiving updates
     */
    void stop();
}
