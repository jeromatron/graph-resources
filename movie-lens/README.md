Movie Lens Playground
=====================

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

start the gremlin shell

```
:load MovieLensParser.groovy
graph = TinkerGraph.open()
MovieLensParser.load(graph, '/tmp/ml-1m')
graph.io(IoCore.gryo()).writeGraph("/tmp/movie-lens.kryo")
```

Example execution

```
gremlin> :load MovieLensParser.groovy
==>true
gremlin> graph = TinkerGraph.open()
==>tinkergraph[vertices:0 edges:0]
gremlin> MovieLensParser.load(graph, '/tmp/ml-1m')
Processing movies.dat...
Processing users.dat...
Processing ratings.dat...
Loading took (ms): 14273
==>null
gremlin> graph.io(IoCore.gryo()).writeGraph("/tmp/movie-lens.kryo")
==>null
```

References
----------

- Original script: [https://gist.github.com/dkuppitz/64a9f1ba30ca652d067a](https://gist.github.com/dkuppitz/64a9f1ba30ca652d067a)
