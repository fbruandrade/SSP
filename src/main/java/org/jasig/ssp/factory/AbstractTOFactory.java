package org.jasig.ssp.factory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.transferobject.TransferObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class AbstractTOFactory<M, TObject extends TransferObject<M>> {
	private final transient Class<TObject> tObjectClass;

	protected final transient Class<M> mClass;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractAuditableTOFactory.class);

	/**
	 * Constructor that initializes specific class instances for use by the
	 * common base class methods.
	 * 
	 * @param tObjectClass
	 *            Transfer object class for the associated model class
	 * @param mClass
	 *            The model class
	 */
	public AbstractTOFactory(final Class<TObject> tObjectClass,
			final Class<M> mClass) {
		this.tObjectClass = tObjectClass;
		this.mClass = mClass;
	}

	public TObject from(final M model) {
		final TObject tObject = newTObject();
		tObject.from(model);
		return tObject;
	}

	public List<TObject> asTOList(final Collection<M> models) {
		final List<TObject> tos = Lists.newArrayList();

		if ((models != null) && !models.isEmpty()) {
			for (M model : models) {
				tos.add(from(model));
			}
		}

		return tos;
	}

	public Set<TObject> asTOSet(final Collection<M> models) {
		final Set<TObject> tos = Sets.newHashSet();
		for (M model : models) {
			tos.add(from(model));
		}
		return tos;
	}

	public Set<M> asSet(final Collection<TObject> tObjects)
			throws ObjectNotFoundException {
		final Set<M> models = Sets.newHashSet();
		for (TObject tObject : tObjects) {
			models.add(from(tObject));
		}
		return models;
	}

	protected TObject newTObject() {
		try {
			return tObjectClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("unable to instantiate Transfer object in factory.");
		} catch (IllegalAccessException e) {
			LOGGER.error("unable to instantiate Transfer object in factory.");
		}
		return null;
	}

	protected M newModel() {
		try {
			return mClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("unable to instantiate Model object in factory.");
		} catch (IllegalAccessException e) {
			LOGGER.error("unable to instantiate Model object in factory.");
		}

		return null;
	}

	public abstract M from(final TObject tObject)
			throws ObjectNotFoundException;
}
