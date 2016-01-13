Movie Lens
==========

The movie lens data set will give you ratings that you can import into a graph using the included groovy script.
The script will need be run through the gremlin shell to import and then serialize to kryo format.

Data set
--------

- One million ratings from 6000 users on 4000 movies
- [http://grouplens.org/datasets/movielens/1m/](http://grouplens.org/datasets/movielens/1m/)  

How to run
----------

```
cd /tmp
wget http://files.grouplens.org/datasets/movielens/ml-1m.zip
unzip ml-1m.zip
cd $OLDPWD
```

Example execution (in the gremlin shell)

```
gremlin> :load MovieLensParser.groovy
==>true
gremlin> graph = TinkerGraph.open()
==>tinkergraph[vertices:0 edges:0]
gremlin> MovieLensParser.load(graph, '/tmp/ml-1m')
Adding occupation vertices...
Processing movies.dat...
Processing users.dat...
Processing ratings.dat...
Loading took (ms): 14273
==>null
gremlin> graph.io(IoCore.gryo()).writeGraph("/tmp/movie-lens.kryo")
==>null
```

Now you have a serialized graph in kryo format for repeated use.  You can start the gremlin shell, read the graph, and are ready to play with it.

```
gremlin> graph = TinkerGraph.open()
==>tinkergraph[vertices:0 edges:0]
gremlin> graph.io(gryo()).readGraph('/tmp/movie-lens.kryo')
==>null
gremlin> g = graph.traversal()
==>graphtraversalsource[tinkergraph[vertices:9962 edges:1012657], standard]
gremlin> g.V().count()
==>9962
gremlin> g.E().count()
==>1012657
gremlin> 
```

Perhaps try to do something like find the average rating for a particular movie:

```
g.V().has('movie','name','Toy Story').inE('rated').values('stars').mean()
```

Or get the list of genres:

```
g.V().hasLabel('genre').valueMap()
```

Number of movies in each genre:

```
g.V().hasLabel('genre').as('a','b').select('a','b').by('name').by(inE('genre').count())
```

You can see how many of each type of vertex or edge there is:

```
g.V().label().groupCount()
g.E().label().groupCount()
```

What's the year of the oldest and most recent movie in the data set?  How many movies are there for each year?

```
g.V().hasLabel('movie').values('year').min()
g.V().hasLabel('movie').values('year').max()
g.V().hasLabel('movie').groupCount().by('year').order(local).by(keyIncr).unfold()
```

For programmers that gave Die Hard 5 stars, what are the top 10 other movies that they gave 5 stars?

```
g.V().
has('movie','name','Die Hard').as('a').
inE('rated').has('stars',5).
outV().where(out('occupation').has('name','programmer')).
outE('rated').has('stars',5).
inV().where(neq('a')).
groupCount().by('name').
order(local).by(valueDecr).
limit(local,10).
unfold()
```

Reading this from the beginning in English:

> Give me the vertex with the movie name Die Hard.  Take a look at all the edges from users that rated it 5 stars and 
filter the users down by those whose occupation is a programmer.  Take a look at all the movies that those programmers
rated 5 stars.  Filter out 'a' or 'Die Hard' because we already know they rated that 5 stars.  Count those movies
they also rated 5 stars, order them by the count highest to lowest, and limit it to the top ten.  Print each of the
results on their own line.

References
----------

- Presentation by Marko Rodriguez on [The Gremlin Traversal Language](http://www.slideshare.net/slidarko/the-gremlin-traversal-language)
- For the script itself, borrowed from gists from [Marko Rodriguez](https://gist.github.com/okram/d9f158dee789689759da) and [Daniel Kuppitz](https://gist.github.com/dkuppitz/64a9f1ba30ca652d067a)
