import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Type-safe',
    description: (
      <>
          Utilizes Scala's type system to catch bugs at compile time
      </>
    ),
  },
  {
    title: 'Streaming-friendly',
    description: (
      <>
          Naturally integrates with ZIO's feature-rich ZStreams
      </>
    ),
  },
  {
    title: 'ZIO-native',
    description: (
      <>
          Built using ZIO and libraries within it's ecosystem, with ZIO in mind
      </>
    ),
  },
];

function Feature({title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
