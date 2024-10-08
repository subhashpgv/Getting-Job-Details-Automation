package WebScrapers;

public class MainCoordinator {
    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
     
        Class<?>[] scrapers = {
        		  WebScrapers.JobScrapping3.class,
                  WebScrapers.JobScrapping2.class,
                  WebScrapers.JobScrapping1.class,
                  WebScrapers.JobScrapping4.class   
        };

        Thread[] threads = new Thread[scrapers.length];
        
        for (int i = 0; i < scrapers.length; i++) {
            threads[i] = new Thread(new JobScraperTask(scrapers[i], args));
            threads[i].start();
        }

       
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("All scraping tasks completed.");
    }

   
    static class JobScraperTask implements Runnable {
        private Class<?> jobScraperClass;
        private String[] args;

        public JobScraperTask(Class<?> jobScraperClass, String[] args) {
            this.jobScraperClass = jobScraperClass;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                jobScraperClass.getMethod("main", String[].class).invoke(null, (Object) args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}