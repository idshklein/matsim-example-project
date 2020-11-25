library(tidyverse)
library(hms)
library(tidygraph)
library(sf)
library(sfnetworks)
library(igraph)
library(assertr)
# create a network
NODES_NUM = 11
nodes <- data.frame(name = 1:NODES_NUM )
nodes$geom <- st_sfc(lapply(nodes$name, function(x){
  st_point(c((x-1)*1000,0))
}),crs=2039)
nodes <- st_as_sf(nodes)
#edges <- data.frame(from = c(1:(NODES_NUM-1),NODES_NUM:2),
#                    to = c(2:NODES_NUM,(NODES_NUM-1):1))
edges <- data.frame(from = c(1:(NODES_NUM-1),11),
                    to = c(2:NODES_NUM,1))
edges <- edges %>%
  left_join(nodes, by = c("from" = "name")) %>%
  left_join(nodes, by = c("to" = "name")) %>%
  mutate(geom = st_sfc(map2(geom.x,geom.y,~st_linestring(rbind(.x,.y))),crs = 2039)) %>%
  st_set_geometry("geom") %>% 
  select(-geom.x,-geom.y)
 
edges$freespeed <- 30 /3.6
edges$length <- st_length(edges$geom)
edges$capacity <- 500
edges$number_of_lanes <- 1
net <- sfnetwork(nodes,edges,directed = TRUE) 
# create agents
N_AGENTS = 10
persons <- tibble(.rows = N_AGENTS )
persons$id <- 1:N_AGENTS
persons$age <- round(rnorm(N_AGENTS,45,10))
persons$sex <- sample(rep(c("male","female"),N_AGENTS),N_AGENTS)
# create plans - according to order
persons$home <- sample(nodes$name[2:(nrow(nodes)-3)],N_AGENTS,replace = T)
HOUR_OF_DEPARTURE = 60*60*7
NODE_INTERVAL = 120
persons$departure_time <- HOUR_OF_DEPARTURE + (persons$home - 1 )*NODE_INTERVAL + sample(NODE_INTERVAL,nrow(persons))
persons <- persons %>% 
  mutate(work = map(home,~ifelse(.==(nrow(nodes) - 3),
                                 nrow(nodes)-2, 
                                 sample((.+1):(nrow(nodes)-2) ,1)))) %>% 
  unnest(work)
persons %>% 
  verify(work > home)
net %>%
  activate(nodes) %>%
  as_tibble() %>%
  mutate(x = map_dbl(geom,~st_coordinates(.)[1]),
         y = map_dbl(geom,~st_coordinates(.)[2])) %>% 
  st_drop_geometry() %>% 
  write_csv("nodes.csv")
net %>%
  activate(edges) %>%
  as_tibble() %>%
  st_drop_geometry() %>%
  write_csv("edges.csv")
persons %>%
  write_csv("population.csv")