package WebScrapers;


public class testCoordinator {
    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
     
        Class<?>[] scrapers = {
        		  WebScrapers.testscrap3.class,
        		  WebScrapers.testscrap5.class,
        		  WebScrapers.testscrap1.class,
                  WebScrapers.testscrap2.class,
                  WebScrapers.testscrap4.class
                  
        };

        for (Class<?> scraperClass : scrapers) {
            try {
                // Invoke the main method of each scraper class sequentially
                scraperClass.getMethod("main", String[].class).invoke(null, (Object) args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("All scraping tasks completed.");
    }
}
