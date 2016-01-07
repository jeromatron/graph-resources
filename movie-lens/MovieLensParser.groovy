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
                graph.addVertex(id, 'o' + key, label, 'occupation', 'name', value)
        }

        println "Adding genres from movies.dat..."
        def genres = [] as Set
        new File(dataDirectory + '/movies.dat').eachLine {
            it.split("::")[2].split("\\|").each { genres.add(it) }
        }
        genres.each {
            graph.addVertex(label, 'genre', 'name', it)
        }

        println 'Adding movies from movies.dat...'
        // MovieID::Title::Genres
        new File(dataDirectory + '/movies.dat').eachLine {
            line ->
                def components = line.split("::")
                def movieId = new Integer(components[0])
                def movieTitleYear = components[1] =~ /(.*)\s*\((\d+)\)/
                movieTitleYear.find()
                def movieTitle = movieTitleYear.group(1).trim()
                def movieYear = movieTitleYear.group(2).toInteger()
                def movie = graph.addVertex(id, 'm' + movieId, label, 'movie', 'name', movieTitle, 'year', movieYear)
                components[2].split("\\|").each {
                    movie.addEdge('genre', g.V().hasLabel('genre').has('name', it).next())
                }
        }

        println 'Processing users.dat...'
        // UserID::Gender::Age::Occupation::Zipcode
        new File(dataDirectory + '/users.dat').eachLine {
            def components = it.split("::")
            def userId = components[0] as Integer
            def gender = components[1]
            def age = components[2] as Integer
            def occupationId = components[3]
            def zipcode = components[4]
            def user = graph.addVertex(id, 'u' + userId, label, 'user', 'gender', gender, 'age', age, 'zipcode', zipcode)
            user.addEdge('occupation', g.V().hasLabel('occupation').has(id, 'o' + occupationId).next())
        }

        println 'Processing ratings.dat...'
        // UserID::MovieID::Rating::Timestamp
        new File(dataDirectory + '/ratings.dat').eachLine {
            line ->
                def components = line.split("::");
                // Get the user and movie by their ids to add the edge
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