library(tidyverse)
library(fs)
data <- dir_info("D:/one_line_results/debug") %>% 
  filter( modification_time   > "2020-12-14 15:50:00") %>%
  mutate(drt_customer_stats_drt = map(path, ~read_delim(paste0(.,"/drt_customer_stats_drt.csv"),";")),
         start = map_int(path,~max(unlist(str_locate_all(.,"/")))),
         str = str_sub(path,(start+1)),
         str = str_remove(str,"new"),
         str = str_split(str,"_"),
         waitmax = map_chr(str,~.[[3]]),
         waitmax = as.numeric(waitmax),
         alpha = map_chr(str,~.[[5]]),
         alpha = as.numeric(alpha),
         beta = map_chr(str,~.[[7]]),
         beta = as.numeric(beta),
         stop_duration = map_chr(str,~.[[9]]),
         stop_duration = as.numeric(stop_duration)
  ) %>% 
  select(waitmax,alpha,beta,stop_duration,drt_customer_stats_drt) %>%
  unnest(drt_customer_stats_drt)
data %>% 
  filter(beta == 31) %>%
  ggplot(aes(x = factor(waitmax),y=factor(beta),color= rejectionRate,label= rejectionRate)) + 
  # geom_point() +
  geom_text()  +
  labs(x = "waitmax", y = "beta") +
  facet_wrap(~stop_duration,ncol=1)
data %>% 
  ggplot(aes(x = factor(waitmax),y=factor(stop_duration),color= rejectionRate,label= rejectionRate)) + 
  # geom_point() +
  geom_text()  +
  labs(x = "waitmax", y = "stop_duration")

data %>% 
  ggplot(aes(x = factor(beta),y=factor(stop_duration),color= rejectionRate,label= rejectionRate)) + 
  # geom_point() +
  geom_text()  +
  labs(x = "beta", y = "stop_duration")
