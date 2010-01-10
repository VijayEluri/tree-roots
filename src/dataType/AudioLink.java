package dataType;

//contains information describing a link that points to a audio file
public class AudioLink extends FileLink
{
	protected Integer audioId;
	protected String artist;
	protected String album;
	protected String track;
	protected String searchField;

	// the maximum lengths that can be used for artist and track
	private static final int artistMaxLength = 100;
	private static final int trackMaxLength = 100;
	private static final int searchFieldMaxLength = 200;

	public AudioLink(FileLink fileLink)
	{
		super(fileLink.getFileName(), fileLink);
	}
	
	public Integer getAudioId()
	{
		return audioId;
	}

	public void setAudioId(Integer audioId)
	{
		this.audioId = audioId;
	}

	public String getSearchField()
	{
		return searchField;
	}

	public void setSearchField(String searchField)
	{
		// constrain the set artistSearchField to the set length
		searchField = constrainLength(searchField, searchFieldMaxLength);

		// just use lower case for everything
		this.searchField = searchField.toLowerCase();

	}

	public String getAlbum()
	{
		return album;
	}

	public void setAlbum(String album)
	{
		// just use lower case for everything
		this.album = album.toLowerCase().trim();
	}

	public String getArtist()
	{
		return artist;
	}

	public void setArtist(String artist)
	{
		// constrain the set artistSearchField to the set length
		artist = constrainLength(artist, artistMaxLength);

		// just use lower case for everything
		this.artist = artist.toLowerCase().trim();
	}

	public String getTrack()
	{
		return track;
	}

	public void setTrack(String track)
	{
		// constrain the set artistSearchField to the set length
		track = constrainLength(track, trackMaxLength);

		// just use lower case for everything
		this.track = track.toLowerCase().trim();
	}
	
	private String constrainLength(String in, int len)
	{
		// constrain the set artistSearchField to the set length
		if (in.length() > len)
			in = in.substring(0, len - 1);

		return in;
	}
}
