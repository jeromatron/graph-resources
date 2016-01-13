import org.apache.tinkerpop.gremlin.structure.Graph

/**
 * Loads the movie lens dataset into a given graph.  See readme for more information.
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
                graph.addVertex(label, 'occupation', 'uid', 'o' + key, 'name', value)
        }

        println 'Processing movies.dat...'
        // MovieID::Title::Genres
        new File(dataDirectory + '/movies.dat').eachLine {
            line ->
                def components = line.split("::")
                def movieId = new Integer(components[0])
                def movieTitleYear = components[1] =~ /(.*)\s*\((\d+)\)/
                movieTitleYear.find()
                def movieTitle = movieTitleYear.group(1).trim()
                def movieYear = movieTitleYear.group(2).toInteger()
                def movieVertex = graph.addVertex(label, 'movie', 'uid', 'm' + movieId, 'name', movieTitle, 'year', movieYear)
                components[2].split("\\|").each { def genre ->
                    def genreVertex = g.V().has('uid', 'g' + genre).tryNext().orElseGet {graph.addVertex(label, 'genre', 'uid', 'g' + genre, 'name', genre)}
                    movieVertex.addEdge('genre', genreVertex)
                }
        }

        println 'Processing users.dat...'
        // UserID::Gender::Age::Occupation::Zipcode
        new File(dataDirectory + '/users.dat').eachLine {
            def components = it.split("::")
            def userId = components[0].toInteger()
            def gender = components[1]
            def age = components[2].toInteger()
            def occupationId = components[3].toInteger()
            def zipcode = components[4]
            def userVertex = graph.addVertex(label, 'user', 'uid', 'u' + userId, 'gender', gender, 'age', age, 'zipcode', zipcode)

            def occupationVertex = g.V().has('uid', 'o' + occupationId).next()
            userVertex.addEdge('occupation', occupationVertex)
        }

        println 'Processing ratings.dat...'
        // UserID::MovieID::Rating::Timestamp
        new File(dataDirectory + '/ratings.dat').eachLine {
            line ->
                def components = line.split("::");
                def userId = components[0].toInteger()
                def movieId = components[1].toInteger()
                def stars = components[2].toInteger()
                def time = components[3].toLong()
                // Get the user and movie by their ids to add the edge
                def userTraversal = g.V().has('uid', 'u' + userId)
                def movieTraversal = g.V().has('uid', 'm' + movieId)
                if (userTraversal.hasNext() && movieTraversal.hasNext()) {
                    userTraversal.next().addEdge('rated', movieTraversal.next(), 'stars', stars, 'time', time)
                }
        }
    }

    public static void load(final Graph graph, final String dataDirectory) {
        //ToDo: Use a case statement for each database type, all will require an index
        graph.createIndex('uid', Vertex.class)
        def start = System.currentTimeMillis()
        parse(graph, dataDirectory)
        println "Loading took (ms): " + (System.currentTimeMillis() - start)
    }
}