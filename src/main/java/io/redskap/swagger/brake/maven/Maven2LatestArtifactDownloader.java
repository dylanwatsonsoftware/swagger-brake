package io.redskap.swagger.brake.maven;

import static java.lang.String.format;

import java.io.File;

import io.redskap.swagger.brake.maven.model.MavenMetadata;
import io.redskap.swagger.brake.maven.model.MavenSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Maven2LatestArtifactDownloader implements LatestArtifactDownloader {
    private final MavenMetadataDownloader metadataDownloader;
    private final TemporaryJarFileDownloader temporaryJarFileDownloader;

    @Override
    public File download(String repoUrl, String groupId, String artifactId) {
        String groupPath = groupId.replaceAll("\\.", "/");
        String artifactBaseUrl = format("%s/%s/%s", repoUrl, groupPath, artifactId);
        String latestVersion = getLatestArtifactVersion(artifactBaseUrl);
        String latestSnapshotName = getLatestSnapshotName(artifactId, artifactBaseUrl, latestVersion);
        String latestArtifactUrl = format("%s/%s/%s.jar", artifactBaseUrl, latestVersion, latestSnapshotName);
        return temporaryJarFileDownloader.download(groupId, artifactId, latestVersion, latestArtifactUrl);
    }

    private String getLatestSnapshotName(String artifactId, String artifactUrl, String latestVersion) {
        MavenMetadata snapshotMetadata = metadataDownloader.download(format("%s/%s", artifactUrl, latestVersion));
        MavenSnapshot snapshot = snapshotMetadata.getVersioning().getSnapshot();
        String snapshotVersion = latestVersion.replaceAll("SNAPSHOT", snapshot.getTimestamp());
        return format("%s-%s-%s", artifactId, snapshotVersion, snapshot.getBuildNumber());
    }

    private String getLatestArtifactVersion(String artifactUrl) {
        MavenMetadata mavenMetadata = metadataDownloader.download(artifactUrl);
        return mavenMetadata.getVersioning().getLatest();
    }
}