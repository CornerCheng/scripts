#!/usr/bin/env filebot -script


def specials = any{ specials.toBoolean() }{ false }

def episodes = [] as LinkedHashSet
def shows = [] as LinkedHashSet

args.getFiles().each{ f ->
	if (f.isVideo()) {
		def episode = f.metadata
		def seriesInfo = any{ episode.seriesInfo }{ null }

		if (episode instanceof Episode && seriesInfo instanceof SeriesInfo) {
			log.finest "$seriesInfo | $episode | $f"

			shows += seriesInfo

			if (episode instanceof MultiEpisode) {
				episodes += episode.episodes as List
			} else {
				episodes += episode
			}
		}
	}
}


// sanity checks
if (!episodes) {
	die "Illegal usage: input folder does not contain xattr tagged files"
}


def queries = shows.collect{
	[id: it.id, database: it.database, order: it.order as SortOrder, language: it.language as Locale]
} as LinkedHashSet

def episodeList = queries.collectMany{
	WebServices.getEpisodeListProvider(it.database).getEpisodeList(it.id, it.order, it.language)
} as LinkedHashSet


// ignore specials
if (!specials) {
	episodeList = episodeList.findAll{ it.special == null }
}


// print missing episodes
def missingEpisodes = episodeList - episodes

missingEpisodes.each{
	println it
}
