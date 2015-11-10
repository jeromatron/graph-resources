Enron Email
===========

The Enron email data set is about half a million data points of a communication network between individuals.  For example, person 75 sent an email to person 2049.  There are 36692 nodes and 367662 edges.  The link to the data set is [here](https://snap.stanford.edu/data/email-Enron.html).

Getting the data set
--------------------

```
cd /tmp
wget https://snap.stanford.edu/data/email-Enron.txt.gz
gzip -d email-Enron.txt.gz 
cd $OLDPWD
```

Load the data (in the Gremlin shell)
------------------------------------

```
gremlin> :load EnronEmailParser.groovy
==> Lots of classpath stuff
==>true
==>true
gremlin> graph = TinkerGraph.open()
==>tinkergraph[vertices:0 edges:0]
gremlin> EnronEmailParser.load(graph, '/tmp/email-Enron.txt')
==>null
gremlin> g = graph.traversal()
==>graphtraversalsource[tinkergraph[vertices:36692 edges:367662], standard]
gremlin>
```

