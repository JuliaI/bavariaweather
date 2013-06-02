package de.juliai.bavariaweather;

import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

/**
 * 
 * @author JuliaI
 * 
 */
public class RegionsAdapter implements Adapter {

	private List<Region> regions;

	/**
	 * constructor
	 * 
	 * @param regions
	 */
	public RegionsAdapter(final List<Region> regions) {
		this.regions = regions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getItem(int position) {
		if (position < 0 || position >= regions.size()) {
			position = 0;
		}

		return regions.get(position).getWeatherData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < 0 || position >= regions.size()) {
			position = 0;
		}

		return regions.get(position).getView();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCount() {
		return regions.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return regions.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getViewTypeCount() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasStableIds() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}
}
