import org.apache.tinkerpop.gremlin.structure.Graph

/**
 * See https://gist.github.com/okram/d9f158dee789689759da and https://gist.github.com/dkuppitz/64a9f1ba30ca652d067a
 * Dataset: http://grouplens.org/datasets/movielens/1m/
 *
 * gremlin> :load /tmp/MovieLensParser.groovy
 * ==>true
 * gremlin> graph = TinkerGraph.open()
 * ==>tinkergraph[vertices:0 edges:0]
 * gremlin> MovieLensParser.load(graph, '/tmp/ml-1m')
 * Adding occupation vertices...
 * Adding categories from movies.dat...
 * Adding movies from movies.dat...
 * Processing users.dat...
 * Processing ratings.dat...
 * Loading took (ms): 12116
 * ==>null
 * gremlin> graph.io(IoCore.gryo()).writeGraph("/tmp/movie-lens.kryo")
 * ==>null
 *
 */
class MovieLensParser {

    static Map occupations

    static {
        occupations = [0 : 'other', 1: 'academic/educator', 2: 'artist',
                       3 : 'clerical/admin', 4: 'college/grad student', 5: 'customer service',
                       6 : 'doctor/health care', 7: 'executive/managerial', 8: 'farmer',
                       9 : 'homemaker', 10: 'K-12 student', 11: 'lawyer', 12: 'programmer',
                       13: 'retired', 14: 'sales/marketing', 15: 'scientist', 16: 'self-employed',
                       17: 'technician/engineer', 18: 'tradesman/craftsman', 19: 'unemployed', 20: 'writer']
    }

    public static void parse(final Graph graph, final String dataDirectory) {

        def g = graph.traversal()

        println "Adding occupation vertices..."
        occupations.each {
            key, value ->
                graph.addVertex(id, 'o' + key, label, 'occupation', 'name', value)
        }

        println "Adding categories from movies.dat..."
        def categories = [] as Set
        new File(dataDirectory + '/movies.dat').eachLine {
            it.split("::")[2].split("\\|").each { categories.add(it) }
        }
        categories.each {
            graph.addVertex(label, 'category', 'name', it)
        }

        println 'Adding movies from movies.dat...'
        // MovieID::Title::Genres
        new File(dataDirectory + '/movies.dat').eachLine {
            line ->
                def components = line.split("::")
                def movieId = new Integer(components[0])
                def movieTitleYear = components[1] =~ /(.*)\s*\((\d+)\)/
                movieTitleYear.find()
                def movie = graph.addVertex(id, 'm' + movieId, label, 'movie', 'name', movieTitleYear.group(1).trim(), 'year', movieTitleYear.group(2) as Integer)
                components[2].split("\\|").each {
                    movie.addEdge('category', g.V().hasLabel('category').has('name', it).next())
                }
        }

        println 'Processing users.dat...'
        // UserID::Gender::Age::Occupation::Zip-code
        new File(dataDirectory + '/users.dat').eachLine {
            def user = graph.addVertex(id, 'u' + (it.split("::")[0] as Integer), label, 'user', 'gender', it.split("::")[1], 'age', (it.split("::")[2] as Integer))
            user.addEdge('occupation', g.V().hasLabel('occupation').has(id, 'o' + it.split("::")[3]).next())
        }

        println 'Processing ratings.dat...'
        // UserID::MovieID::Rating::Timestamp
        new File(dataDirectory + '/ratings.dat').eachLine {
            line ->
                def components = line.split("::");
                def user = g.V('u' + (components[0] as Integer)).next()
                def movie = g.V('m' + (components[1] as Integer)).next()
                user.addEdge('rated', movie, 'stars', components[2] as Integer)
        }
    }

    public static void load(final Graph graph, final String dataDirectory) {
        def start = System.currentTimeMillis()
        parse(graph, dataDirectory)
        println "Loading took (ms): " + (System.currentTimeMillis() - start)
    }
}