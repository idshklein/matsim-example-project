library(tidyverse)
library(hms)
library(tidygraph)
library(sf)
library(sfnetworks)
library(igraph)
library(assertr)
# create a network
#NODES_NUM = 11
#DISTANCE_BETWEEN_NODES <- 1000
#SPEED_ON_LINKS <- 30 /3.6
#CAPACITY_ON_LINKS <- 500
#NUMBER_OF_LANES <- 1
#STOP_DURATION <- 60
nodes <- data.frame(name = 1:NODES_NUM )
nodes$geom <- st_sfc(lapply(nodes$name, function(x){
					st_point(c((x-1)*DISTANCE_BETWEEN_NODES,0))
				}),crs=2039)
nodes <- st_as_sf(nodes)

edges <- data.frame(from = c(1:(NODES_NUM-1),NODES_NUM),
		to = c(2:NODES_NUM,1))
edges <- edges %>%
		left_join(nodes, by = c("from" = "name")) %>%
		left_join(nodes, by = c("to" = "name")) %>%
		mutate(geom = st_sfc(map2(geom.x,geom.y,~st_linestring(rbind(.x,.y))),crs = 2039)) %>%
		st_set_geometry("geom") %>% 
		select(-geom.x,-geom.y)

edges$freespeed <- SPEED_ON_LINKS
edges$length <- st_length(edges$geom)
edges$capacity <- CAPACITY_ON_LINKS
edges$number_of_lanes <- NUMBER_OF_LANES

# create agents
N_AGENTS <-  10
MEAN_AGE <- 45
persons <- tibble(.rows = N_AGENTS )
persons$id <- 1:N_AGENTS
persons$age <- round(rnorm(N_AGENTS,MEAN_AGE,10))
persons$sex <- sample(rep(c("male","female"),N_AGENTS),N_AGENTS)
# create plans - according to order
# should be paramaterized
#persons$home <- sample(nodes$name[2:(nrow(nodes)-3)],N_AGENTS,replace = T)
persons$home <- 1:10
HOUR_OF_DEPARTURE <- 60*60*7
NODE_INTERVAL = DISTANCE_BETWEEN_NODES / SPEED_ON_LINKS
persons$departure_time <- HOUR_OF_DEPARTURE + (persons$home - 1 )*NODE_INTERVAL + STOP_DURATION*0:9
st_x = function(x) st_coordinates(x)[,1]
st_y = function(x) st_coordinates(x)[,2]
persons <- persons %>% 
#  mutate(work = map(home,~ifelse(.==(nrow(nodes) - 3),
#                                 nrow(nodes)-2, 
#                                 sample((.+1):(nrow(nodes)-2) ,1)))) %>%
		mutate(work = 11) %>%
		unnest(work) %>% 
		left_join(nodes,by = c("home" = "name")) %>%
		rename(home_coords = geom) %>%
		mutate(home_x = st_x(home_coords),
				home_y = st_y(home_coords)) %>%
		left_join(nodes,by = c("work" = "name")) %>%
		rename(work_coords = geom) %>%
		mutate(work_x = st_x(work_coords),
				work_y = st_y(work_coords)) %>%
		select(-home_coords,-work_coords)
persons %>% 
		verify(work > home)

drt <- data.frame(time = HOUR_OF_DEPARTURE,location = nodes[1,])
edges <- edges %>% 
		mutate(is_pickup_or_dropoff = from %in% persons$home | to %in% persons$work,
				time_between_nodes =NODE_INTERVAL, 
				time_between_nodes_cumsum = cumsum(time_between_nodes) - NODE_INTERVAL,
				stop_duration_cumsum = cumsum(is_pickup_or_dropoff*STOP_DURATION),
				estimated_time = HOUR_OF_DEPARTURE + time_between_nodes_cumsum + stop_duration_cumsum,
				hms = hms::hms(estimated_time))

net <- sfnetwork(nodes,edges,directed = TRUE) 
net %>%
		activate(nodes) %>%
		as_tibble() %>%
		mutate(x = map_dbl(geom,~st_coordinates(.)[1]),
				y = map_dbl(geom,~st_coordinates(.)[2])) %>% 
		st_drop_geometry() %>% 
		write_csv("scenarios/straight_line_drt/nodes.csv")
net %>%
		activate(edges) %>%
		as_tibble() %>%
		st_drop_geometry() %>%
		write_csv("scenarios/straight_line_drt/edges.csv")
persons %>%
		write_csv("scenarios/straight_line_drt/population.csv")